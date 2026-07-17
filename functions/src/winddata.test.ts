import assert from 'node:assert/strict';
import test from 'node:test';

import {
  extractBrieData,
  extractGruyData,
  extractNeucData,
  extractScniData,
  extractWsctData,
} from './winddata';

test('extractNeucData parses wind measurements, direction, and time', () => {
  const data = JSON.stringify({
    windSpeedKnotsIchtus: '12.6',
    windSpeedHigh1KnotsIchtus: '18.4',
    windDirectionDegreesIchtus: 90,
    recordTimeIchtus: '2026-01-15T08:05:00',
  });

  assert.deepEqual(extractNeucData(data), {
    force: 13,
    gust: 18,
    direction: 'E',
    time: '08:05',
  });
});

test('extractScniData returns the latest point and direction', () => {
  const data = JSON.stringify({
    data: {
      data: [
        {x: '2026-07-01 08:00:00', y: '12'},
        {x: '2026-07-01 09:00:00', y: '18.52'},
      ],
      hourlyDir:
        '<span title="01.07.2026 08:00 : S"></span><span title="01.07.2026 09:00 : SSE"></span>',
    },
  });

  assert.deepEqual(extractScniData(data), {
    force: 10,
    gust: 0,
    direction: 'SSE',
    time: '11:00',
  });
});

test('extractScniData returns an empty reading without wind points', () => {
  assert.deepEqual(extractScniData(JSON.stringify({data: {data: []}})), {
    force: 0,
    gust: 0,
    direction: '',
    time: '',
  });
});

test('extractWsctData parses XML and falls back invalid wind speed to zero', () => {
  const data = [
    '<windkts>bad</windkts>',
    '<windgustkts>14.6</windgustkts>',
    '<curval_winddir>315</curval_winddir>',
    '<time>09:45</time>',
  ].join('');

  assert.deepEqual(extractWsctData(data), {
    force: 0,
    gust: 15,
    direction: 'NW',
    time: '09:45',
  });
});

test('extractBrieData parses the classic station table', () => {
  const data = `
    <table id="table-2">
      <tr><th colspan="4">Updated at 10:30</th></tr>
      <tr><td>Wind aktuell</td><td>18.52 km/h</td></tr>
      <tr><td>Wind-B�e</td><td>25.93 km/h</td></tr>
      <tr><td>Wind-Richtung</td><td>SW</td></tr>
    </table>`;

  assert.deepEqual(extractBrieData(data), {
    force: 10,
    gust: 14,
    direction: 'SW',
    time: '10:30',
  });
});

test('extractBrieData returns an empty reading without the station table', () => {
  assert.deepEqual(extractBrieData('<main>Unavailable</main>'), {
    force: 0,
    gust: 0,
    direction: '',
    time: '',
  });
});

test('extractGruyData selects the newest measurement', () => {
  const data = JSON.stringify({
    measures: [
      {
        updatedAt: '2026-07-01T08:15:00Z',
        windSpeed: 7.2,
        windBurst: 10.4,
        windDir: 180,
      },
      {
        updatedAt: '2026-07-01T09:15:00Z',
        windSpeed: 12.4,
        windBurst: 18.6,
        windDir: 225,
      },
    ],
  });

  assert.deepEqual(extractGruyData(data), {
    force: 12,
    gust: 19,
    direction: 'SW',
    time: '11:15',
  });
});
