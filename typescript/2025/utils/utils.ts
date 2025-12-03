import { readFileSync } from "fs";
import path, { dirname } from "path";
import { fileURLToPath } from "url";

export function readLines(callerUrl: string, filename: string) {
  const callerDir = dirname(fileURLToPath(callerUrl));
  const input = readFileSync(path.join(callerDir, filename), "utf8");
  return input.trim().split(/\r?\n/);
}
