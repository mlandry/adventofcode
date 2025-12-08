import { readLines } from "../utils/utils";

class Equation {
  constructor(public readonly numbers: number[], public readonly operator: string) {}

  solve(): number {
    if (this.operator === "+") {
      return this.numbers.reduce((a, b) => a + b, 0);
    } else if (this.operator === "*") {
      return this.numbers.reduce((a, b) => a * b, 1);
    } else {
      throw new Error(`Unknown operator: ${this.operator}`);
    }
  }
}

const lines = readLines(import.meta.url, "example.txt");

const values = lines.map(line => line.split(/\s+/).filter(line => line.length > 0));
const numbers = values.slice(0, values.length - 1).map(line => line.map(v => Number.parseInt(v)));
const operators = values[values.length - 1];

let equations: Equation[] = [];
for (let i = 0; i < numbers[0].length; i++) {
  equations.push(new Equation(numbers.map(row => row[i]), operators[i]));
}

let total = equations.reduce((acc, equation) => acc + equation.solve(), 0);
console.log("Part 1: " + total);

const startColumns: number[] = [];
for (let i = 0; i < lines[lines.length - 1].length; i++) {
  if (lines[lines.length - 1][i] === '+' || lines[lines.length - 1][i] === '*') {
    startColumns.push(i);
  }
}

const numberLines = lines.slice(0, lines.length - 1);
const maxlen = numberLines.map(line => line.length).reduce((a, b) => Math.max(a, b), 0);

equations = [];
for (let s = 0; s < startColumns.length; s++) {
  const start = startColumns[s];
  const end = s < startColumns.length - 1 ? startColumns[s + 1] : maxlen;
  const nums: number[] = [];
  for (let i = start; i < end; i++) {
    const str = numberLines.map(line => line[i]).filter(v => v !== undefined).join('').trim();
    if (str.length === 0) {
      continue;
    }
    nums.push(Number.parseInt(str));
  }
  equations.push(new Equation(nums, operators[s]));
}
total = equations.reduce((acc, equation) => acc + equation.solve(), 0);
console.log("Part 2: " + total);