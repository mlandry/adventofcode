import { readFileSync } from "fs";
import path, { dirname } from "path";
import { fileURLToPath } from "url";
import { readLines } from "../utils/utils";

const lines = readLines(import.meta.url, "input.txt");

let position = 50;
let zeroCount = 0;

for (const line of lines) {
  const multiplier = line.charAt(0) === "R" ? 1 : -1;
  const distance = parseInt(line.slice(1));
  position = position + multiplier * distance;
  position = ((position % 100) + 100) % 100;
  if (position === 0) {
    zeroCount++;
  }
}

console.log("Part 1: " + zeroCount);

position = 50;
zeroCount = 0;

for (const line of lines) {
  const multiplier = line.charAt(0) === "R" ? 1 : -1;
  const distance = parseInt(line.slice(1));
  for (let i = 0; i < distance; i++) {
    position = position + multiplier;
    position = ((position % 100) + 100) % 100;
    if (position === 0) {
      zeroCount++;
    }
  }
}

console.log("Part 2: " + zeroCount);
