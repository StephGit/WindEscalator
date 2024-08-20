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

function parseDirection(degrees: number): Direction {
  const closestDirection = directionValues.find(
    (direction) =>
      directionMap[direction] - 23 < degrees &&
      directionMap[direction] + 23 > degrees
  );
  return closestDirection || Direction.N;
}

function parseTime(timeString: string): string {
  return DateTime.fromISO(timeString).toFormat('HH:mm:ss');
}


function parseWindSpeed(speedString: string, unit: string): number {
  if (unit === 'km/h') {
    return Math.round(parseFloat(speedString) / 1.852); // Convert km/h to knots
  }
  return Math.round(parseFloat(speedString)); // Assume knots
}

// Function to extract data from JSON
export function extractNeucData(data: string): WindData {
  const json = JSON.parse(data);
  return {
    force: parseWindSpeed(json.windSpeedKnotsIchtus.toString(), 'knots'),
    direction: parseDirection(json.windDirectionDegreesIchtus),
    time: parseTime(json.recordTimeIchtus),
  };
}

// Function to extract data from TXT
export function extractScniData(data: string): WindData {
  let windData: WindData = { force: 0, direction: '', time: '' };
  const lines = data.split('\r\n');
  const dataCols = lines[lines.length - 2].split(' ').filter(n => n)
  const pos = dataCols.length - 15;

  if (dataCols.length > 0) {
    if (isActualData(dataCols[pos])) {
      windData.force = parseWindSpeed(dataCols[pos + 2], 'km/h');
      windData.direction = parseDirection(parseInt(dataCols[pos + 1]));
      windData.time = dataCols[pos];
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
    force: parseWindSpeed(knots, 'knots'),
    direction: parseDirection(parseInt(degrees)),
    time: time,
  };
}
