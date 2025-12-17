import { readLines } from "../utils/utils";

type IndicatorLights = string;
type ButtonWiringSchematic = number[];
type JoltageRequirement = number[];
type JoltageCacheKey = string;

type Procedure = {
  lights: IndicatorLights;
  buttons: ButtonWiringSchematic[];
  joltage: JoltageRequirement;
};

const parseProcedure = (line: string): Procedure => {
  const sp = line.split(" ");
  return {
    lights: sp[0].slice(1, -1),
    buttons: sp.slice(1, -1).map((w) =>
      w
        .slice(1, -1)
        .split(",")
        .map((s) => parseInt(s))
    ),
    joltage: sp[sp.length - 1]
      .slice(1, -1)
      .split(",")
      .map((s) => parseInt(s)),
  };
};

const toggleLightsArray = (
  lights: boolean[],
  button: ButtonWiringSchematic
) => {
  for (const b of button) {
    lights[b] = !lights[b];
  }
};

const findButtonPressCombinations = (
  buttons: ButtonWiringSchematic[],
  goal: IndicatorLights
): ButtonWiringSchematic[][] => {
  // Try every combination of button presses (2^n).
  const combinations: ButtonWiringSchematic[][] = [];
  for (let i = 0; i < 1 << buttons.length; i++) {
    const combination: ButtonWiringSchematic[] = [];
    const lights = new Array(goal.length).fill(false);
    for (let j = 0; j < buttons.length; j++) {
      if (i & (1 << j)) {
        toggleLightsArray(lights, buttons[j]);
        combination.push(buttons[j]);
      }
    }
    const final = lights.map((c) => (c ? "#" : ".")).join("");
    if (final === goal) {
      combinations.push(combination);
    }
  }
  return combinations;
};

const findFewestLightPresses = (procedure: Procedure): number => {
  const combinations = findButtonPressCombinations(
    procedure.buttons,
    procedure.lights
  );
  return combinations
    .map((c) => c.length)
    .reduce((a, b) => Math.min(a, b), Infinity);
};

const procedures = readLines(import.meta.url, "input.txt").map(parseProcedure);

const lightsResult = procedures
  .map(findFewestLightPresses)
  .reduce((a, b) => a + b, 0);
console.log("Part 1: " + lightsResult);

// Part 2 solution inspired by https://www.reddit.com/r/adventofcode/comments/1pk87hl/2025_day_10_part_2_bifurcate_your_way_to_victory/

const findFewestJoltagePressesRecursive = (
  buttons: ButtonWiringSchematic[],
  joltage: JoltageRequirement,
  cache: Map<JoltageCacheKey, number>,
  comboCache: Map<IndicatorLights, ButtonWiringSchematic[][]>
): number => {
  if (joltage.every((j) => j === 0)) {
    return 0;
  }
  const cacheKey = joltage.join(",") as JoltageCacheKey;
  if (cache.has(cacheKey)) {
    return cache.get(cacheKey)!;
  }
  let allEventResult = Infinity;
  if (joltage.every((j) => j % 2 === 0)) {
    allEventResult =
      2 *
      findFewestJoltagePressesRecursive(
        buttons,
        joltage.map((j) => j / 2),
        cache,
        comboCache
      );
  }
  // Otherwise, convert to a light pattern and find all button combinations that could produce that pattern.
  // The key insight is that other buttons may be pushed an even number of times but they won't affect the pattern.
  const pattern = joltage.map((j) => (j % 2 === 0 ? "." : "#")).join("");
  let combinations: ButtonWiringSchematic[][] | undefined = comboCache.get(pattern);
  if (combinations === undefined) {
    combinations = findButtonPressCombinations(buttons, pattern);
    comboCache.set(pattern, combinations);
  }
  const comboResult = combinations
    .filter((c) => c.length !== 0)
    .map((c) => {
      const newJoltage = [...joltage];
      for (const b of c) {
        for (const i of b) {
          newJoltage[i] = newJoltage[i] - 1;
        }
      }
      if (newJoltage.some((j) => j < 0)) {
        return Infinity;
      }
      return (
        c.length + findFewestJoltagePressesRecursive(buttons, newJoltage, cache, comboCache)
      );
    })
    .reduce((a, b) => Math.min(a, b), Infinity);
  const result = Math.min(allEventResult, comboResult);
  cache.set(cacheKey, result);
  return result;
};

const findFewestJoltagePresses = (procedure: Procedure): number => {
  const cache = new Map<JoltageCacheKey, number>();
  const comboCache = new Map<IndicatorLights, ButtonWiringSchematic[][]>();
  const result = findFewestJoltagePressesRecursive(
    procedure.buttons,
    procedure.joltage,
    cache,
    comboCache
  );
  return result;
};

const joltageResult = procedures
  .map(findFewestJoltagePresses)
  .reduce((a, b) => a + b, 0);
console.log("Part 2: " + joltageResult);
