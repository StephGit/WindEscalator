import {DateTime} from 'luxon';

// Define the WindData interface
export interface WindData {
  force: number;
  direction: string;
  time: string;
}

enum Direction {
  E = 'E',
  SE = 'SE',
  S = 'S',
  SW = 'SW',
  W = 'W',
  NW = 'NW',
  N = 'N',
  NE = 'NE',
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

// Function to extract latest wind/gust from meteomap.cloud chart JSON
export function extractScniData(windJson: string): WindData {
  const windResponse = JSON.parse(windJson);

  const windPoints = windResponse?.data?.data ?? [];
  const lastWind = windPoints[windPoints.length - 1];

  if (!lastWind) {
    return {force: 0, direction: '', time: ''};
  }

  // Extract direction from hourlyDir HTML (title="27.04.2026 09:00 : SSE")
  const hourlyDir: string = windResponse?.data?.hourlyDir ?? '';
  const dirMatches = hourlyDir.match(/title="[^"]*"/g) ?? [];
  const lastDirMatch = dirMatches[dirMatches.length - 1] ?? '';
  const dirParts = lastDirMatch.split(' : ');
  const direction =
    dirParts.length > 1 ? dirParts[dirParts.length - 1].replace('"', '') : '';

  return {
    force: parseWindSpeed(lastWind.y.toString(), 'km/h'),
    direction,
    time: DateTime.fromSQL(lastWind.x, {zone: 'utc'})
      .setZone('Europe/Zurich')
      .toFormat('HH:mm:ss'),
  };
}

// Function to extract data from XML using regex
export function extractWsctData(data: string): WindData {
  const knots = data.match(/<windkts>([^<]+)<\/windkts>/)?.[1] ?? '';
  const degrees =
    data.match(/<curval_winddir>([^<]+)<\/curval_winddir>/)?.[1] ?? '';
  const time = data.match(/<time>([^<]+)<\/time>/)?.[1] ?? '';

  return {
    force: parseWindSpeed(knots, 'knots'),
    direction: parseDirection(parseInt(degrees)),
    time: time,
  };
}
