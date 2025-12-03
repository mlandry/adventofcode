import { readLines } from "../utils/utils";

type Range = {
  start: number;
  end: number;
}

function isSilly(id: number) {
  const str = id.toString();
  const len = str.length;
  if (len % 2 !== 0) {
    return false;
  }
  const mid = len / 2;
  const left = str.slice(0, mid);
  const right = str.slice(mid);
  return left === right;
}

function allPartsEqual(str: string, parts: number) {
  const len = str.length / parts;
  const first = str.slice(0, len);
  for (let i = 0; i < parts; i++) {
    const part = str.slice(i * len, (i + 1) * len);
    if (part !== first) {
      return false;
  }
}
  return true;
}

function isInvalid(id: number) {
  const str = id.toString();
  for (let parts = 2; parts <= str.length; parts++) {
    if (str.length % parts !== 0) {
      continue;
    }
    if (allPartsEqual(str, parts)) {
      return true;
    }
  }
  return false;
}

const lines = readLines(import.meta.url, "input.txt");
const ranges: Range[] = lines[0].split(",").map((range) => {
  const [start, end] = range.split("-").map(Number);
  return { start, end };
});

const part1 = ranges.reduce((sum, range) => {
  let local = 0;
  for (let i = range.start; i <= range.end; i++) {
    if (isSilly(i)) {
      local += i;
    }
  }
  return sum + local;
}, 0);

console.log("Part 1: " + part1);

const part2 = ranges.reduce((sum, range) => {
  let local = 0;
  for (let i = range.start; i <= range.end; i++) {
    if (isInvalid(i)) {
      local += i;
    }
  }
  return sum + local;
}, 0);

console.log("Part 2: " + part2);