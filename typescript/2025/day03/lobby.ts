import { readLines } from "../utils/utils";

function findMax(bank: string): { max: number, index: number } {
  let max = 0;
  let index = 0;
  for (let i = 0; i < bank.length; i++) {
    if (bank[i] === '9') {
      return { max: 9, index: i };
    }
    const n = parseInt(bank.charAt(i));
    if (n > max) {
      max = n;
      index = i;
    }
  }
  return { max, index}
}

function maxJoltage(bank: string, digits: number): number[] {
  if (digits === 0) {
    return [];
  }
  const { max, index } = findMax(bank.substring(0, bank.length - digits + 1));
  if (index === -1) {
    return [];
  }
  return [max, ...maxJoltage(bank.substring(index + 1), digits - 1)];
}

const banks = readLines(import.meta.url, "input.txt");

let sum = 0;
for (const bank of banks) {
  const digits = maxJoltage(bank, 2);
  // console.log("bank: " + bank + " maxJoltage: " + digits.join(""));
  sum += parseInt(digits.join(""));
}
console.log("Part 1: " + sum);

sum = 0;
for (const bank of banks) {
  const digits = maxJoltage(bank, 12);
  // console.log("bank: " + bank + " maxJoltage: " + digits.join(""));
  sum += parseInt(digits.join(""));
}
console.log("Part 2: " + sum);