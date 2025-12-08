import { readLines } from "../utils/utils";

const lines = readLines(import.meta.url, "input.txt");
const start = lines[0].indexOf('S');

let beams = new Set<number>([start])
let splits = 0;

for (let row = 1; row < lines.length; row++) {
  const line = lines[row];
  const nextBeams = new Set<number>();
  for (let beam of beams) {
    if (line[beam] === '.') {
      nextBeams.add(beam);
    } else if (line[beam] === '^') {
      splits++;
      if (beam > 0) {
        nextBeams.add(beam - 1);
      }
      if (beam < line.length - 1) {
        nextBeams.add(beam + 1);
      }
    }
  }
  beams = nextBeams;
}

console.log("Part 1: " + splits);

let timelines = new Map<number, number>([[start, 1]])

for (let row = 1; row < lines.length; row++) {
  const line = lines[row];
  const nextTimelines = new Map<number, number>();
  for (let timeline of timelines) {
    if (line[timeline[0]] === '.') {
      const prev = nextTimelines.get(timeline[0]) || 0;
      nextTimelines.set(timeline[0], prev + timeline[1]);
    } else if (line[timeline[0]] === '^') {
      const left = timeline[0] - 1;
      if (left >= 0) {
        const prev = nextTimelines.get(left) || 0;
        nextTimelines.set(left, prev + timeline[1]);
      }
      const right = timeline[0] + 1;
      if (right < line.length) {
        nextTimelines.set(right, nextTimelines.get(right) || 0 + timeline[1]);
      }
    }
  }
  timelines = nextTimelines;
}

console.log("Part 2: " + Array.from(timelines.values()).reduce((acc, timeline) => acc + timeline, 0));

