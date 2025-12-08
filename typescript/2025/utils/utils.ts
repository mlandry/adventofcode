import { readFileSync } from "fs";
import path, { dirname } from "path";
import { fileURLToPath } from "url";

export function readLines(callerUrl: string, filename: string) {
  const callerDir = dirname(fileURLToPath(callerUrl));
  const input = readFileSync(path.join(callerDir, filename), "utf8");
  const lines = input.split(/\r?\n/);
  if (lines[lines.length - 1].length === 0) {
    return lines.slice(0, lines.length - 1);
  }
  return lines;
}
