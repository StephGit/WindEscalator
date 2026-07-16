import {DateTime} from 'luxon';

// Define the WindData interface
export interface WindData {
  force: number;
  gust: number;
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
  return DateTime.fromISO(timeString).toFormat('HH:mm');
}

function parseWindSpeed(speedString: string, unit: string): number {
  const parsed = parseFloat(speedString);

  // Return 0 if parsing fails (NaN, empty string, etc.)
  if (isNaN(parsed)) {
    return 0;
  }

  if (unit === 'km/h') {
    return Math.round(parsed / 1.852); // Convert km/h to knots
  }
  return Math.round(parsed); // Assume knots
}

// Function to extract data from JSON
export function extractNeucData(data: string): WindData {
  const json = JSON.parse(data);
  return {
    force: parseWindSpeed(json.windSpeedKnotsIchtus.toString(), 'knots'),
    gust: parseWindSpeed(json.windSpeedHigh1KnotsIchtus.toString(), 'knots'),
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
    return {force: 0, gust: 0, direction: '', time: ''};
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
    gust: 0,
    direction,
    time: DateTime.fromSQL(lastWind.x, {zone: 'utc'})
      .setZone('Europe/Zurich')
      .toFormat('HH:mm'),
  };
}

// Function to extract data from XML using regex
export function extractWsctData(data: string): WindData {
  const knots = data.match(/<windkts>([^<]+)<\/windkts>/)?.[1] ?? '';
  const gust = data.match(/<windgustkts>([^<]+)<\/windgustkts>/)?.[1] ?? '';
  const degrees =
    data.match(/<curval_winddir>([^<]+)<\/curval_winddir>/)?.[1] ?? '';
  const time = data.match(/<time>([^<]+)<\/time>/)?.[1] ?? '';

  return {
    force: parseWindSpeed(knots, 'knots'),
    gust: parseWindSpeed(gust, 'knots'),
    direction: parseDirection(parseInt(degrees)),
    time: time,
  };
}

// classic soft cheese data extraction
export function extractBrieData(data: string): WindData {
  // Parse HTML and extract table data
  const tableMatch = data.match(
    /<table[^>]*id="table-2"[^>]*>[\s\S]*?<\/table>/
  );
  if (!tableMatch) {
    return {force: 0, gust: 0, direction: '', time: ''};
  }

  const tableHtml = tableMatch[0];
  const rowMatches = tableHtml.match(/<tr[^>]*>[\s\S]*?<\/tr>/g) || [];

  let force = 0;
  let gust = 0;
  let direction = '';
  let time = '';

  for (const row of rowMatches) {
    const cellMatches = row.match(/<td[^>]*>([\s\S]*?)<\/td>/g) || [];
    if (cellMatches.length < 2) continue;

    const sensorName = cellMatches[0]?.replace(/<[^>]*>/g, '').trim() || '';
    const currentValue = cellMatches[1]?.replace(/<[^>]*>/g, '').trim() || '';

    if (sensorName === 'Wind aktuell') {
      force = parseWindSpeed(currentValue.replace('km/h', '').trim(), 'km/h');
    } else if (sensorName === 'Wind-B�e') {
      gust = parseWindSpeed(currentValue.replace('km/h', '').trim(), 'km/h');
    } else if (sensorName === 'Wind-Richtung') {
      direction = currentValue;
    }
  }

  // Extract time from header
  const headerMatch = tableHtml.match(
    /<th[^>]*colspan="4"[^>]*>([\s\S]*?)<\/th>/
  );
  if (headerMatch) {
    const timeMatch = headerMatch[1].match(/\d{2}:\d{2}/);
    time = timeMatch ? timeMatch[0] : '';
  }

  return {force, gust, direction, time};
}

export function extractGruyData(data: string): WindData {
  const json = JSON.parse(data);
  const latest = json.measures.reduce((prev: any, current: any) => {
    return new Date(prev.updatedAt) > new Date(current.updatedAt)
      ? prev
      : current;
  });

  return {
    force: parseWindSpeed(latest.windSpeed.toString(), 'knots'),
    gust: parseWindSpeed(latest.windBurst.toString(), 'knots'),
    direction: parseDirection(latest.windDir),
    time: DateTime.fromISO(latest.updatedAt)
      .setZone('Europe/Zurich')
      .toFormat('HH:mm'),
  };
}
