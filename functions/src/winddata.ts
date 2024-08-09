import { JSDOM } from 'jsdom';
import { DateTime } from 'luxon';

// Define the WindData interface
export interface WindData {
  force: number;
  direction: string;
  time: string;
}

enum Direction {
  E = "E",
  SE = "SE",
  S = "S",
  SW = "SW",
  W = "W",
  NW = "NW",
  N = "N",
  NE = "NE",
}

const directionValues = Object.values(Direction);

const directionMap = {
  [Direction.E]: 90,
  [Direction.SE]: 135,
  [Direction.S]: 180,
  [Direction.SW]: 225,
  [Direction.W]: 270,
  [Direction.NW]: 315,
  [Direction.N]: 0,
  [Direction.NE]: 45,
};

function getByDegree(value: number): Direction {
  const closestDirection = directionValues.find(
    (direction) =>
      directionMap[direction] - 23 < value &&
      directionMap[direction] + 23 > value
  );
  return closestDirection || Direction.N;
}

// Function to extract data from JSON
export function extractNeucData(data: string): WindData {
  const json = JSON.parse(data);
  return {
    force: Math.round(json.windSpeedKnotsIchtus),
    direction: getByDegree(json.windDirectionDegreesIchtus),
    time: DateTime.fromISO(json.recordTimeIchtus).toFormat('HH:mm:ss'),
  };
}

// Function to extract data from TXT
export function extractScniData(data: string): WindData {
  let windData: WindData = { force: 0, direction: '', time: '' };
  const lines = data.split('\r\n');
  const cols = lines[lines.length - 2].split(' ').filter(n => n)
  const pos = cols.length - 15;

  if (cols.length > 0) {
    if (isActualData(cols[pos])) {
      windData.time = cols[pos];
      windData.direction = getByDegree(parseInt(cols[pos + 1]));
      windData.force = calcKnotsByKmh(cols[pos + 2]);
    }
  }

  return windData;
}

// Function to check if data is actual
function isActualData(time: string): boolean {
  const currentHour = DateTime.local({zone: "Europe/Zurich"}).hour;
  return currentHour.toString() === time.split(':')[0];
}

// Function to extract data from HTML (using JSDOM)
export function extractWsctData(data: string): WindData {
  const dom = new JSDOM(data);
  const doc = dom.window.document;
  const knots = doc.querySelector('windkts')?.textContent || '';
  const degrees = doc.querySelector('curval_winddir')?.textContent || '';
  const time = doc.querySelector('time')?.textContent || '';

  return {
    force: Math.round(parseFloat(knots)),
    direction: getByDegree(parseInt(degrees)),
    time,
  };
}

// Function to calculate knots from km/h
function calcKnotsByKmh(windText: string): number {
  return Math.round(parseFloat(windText) / 1.852);
}
