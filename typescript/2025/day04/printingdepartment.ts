import { readLines } from "../utils/utils";

type Point = {
  row: number;
  col: number;
};

class Grid {
  rows: boolean[][];

  constructor(rows: string[]) {
    this.rows = rows.map(row => row.split('').map(c => c === '@'));
  }

  inBounds = (p: Point) => {
    return p.row >= 0 && p.row < this.rows.length && p.col >= 0 && p.col < this.rows[p.row].length;
  }
  
  isPaper = (p: Point) => {
    return this.rows[p.row][p.col];
  }

  getNeighbors = (p: Point) => {
    return [
      { row: p.row - 1, col: p.col - 1 },
      { row: p.row - 1, col: p.col },
      { row: p.row - 1, col: p.col + 1 },
      { row: p.row, col: p.col - 1 },
      { row: p.row, col: p.col + 1 },
      { row: p.row + 1, col: p.col - 1 },
      { row: p.row + 1, col: p.col },
      { row: p.row + 1, col: p.col + 1 },
    ].filter(this.inBounds);
  }

  countAdjacentPaper = (p: Point) => {
    return this.getNeighbors(p).filter(this.isPaper).length;
  }

  removePaper = (p: Point) => {
    this.rows[p.row][p.col] = false;
  }
}

const lines = readLines(import.meta.url, "input.txt");
const grid = new Grid(lines);

let accessible = 0;
for (let row = 0; row < grid.rows.length; row++) {
  for (let col = 0; col < grid.rows[row].length; col++) {
    const p = { row, col };
    if (grid.isPaper(p) && grid.countAdjacentPaper(p) < 4) {
      accessible += 1;
    }
  }
}

console.log("Part 1: %d", accessible);

accessible = 0;
while (true) {
  const toRemove: Point[] = [];
  for (let row = 0; row < grid.rows.length; row++) {
    for (let col = 0; col < grid.rows[row].length; col++) {
      const p = { row, col };
      if (grid.isPaper(p) && grid.countAdjacentPaper(p) < 4) {
        toRemove.push(p);
      }
    }
  }
  if (toRemove.length === 0) {
    break;
  }
  for (const p of toRemove) {
    grid.removePaper(p);
    accessible += 1;
  }
}

console.log("Part 2: %d", accessible);