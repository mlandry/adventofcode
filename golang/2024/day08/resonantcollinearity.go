package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
)

const debug = false

type position struct {
	row int
	col int
}

type vector struct {
	point position
	rd    int
	cd    int
}

func (v *vector) end() position {
	return position{row: v.point.row + v.rd, col: v.point.col + v.cd}
}

func (v *vector) double() *vector {
	return &vector{point: v.point, rd: v.rd * 2, cd: v.cd * 2}
}

func (v *vector) slope() float64 {
	return (float64(v.cd) / float64(v.rd))
}

func print(rows []string, antinodes map[position]bool) {
	if !debug {
		return
	}
	for row, line := range rows {
		for col, c := range line {
			pos := position{row: row, col: col}
			if _, ok := antinodes[pos]; ok {
				fmt.Print(string('#'))
			} else {
				fmt.Print(string(c))
			}
		}
		fmt.Print("\n")
	}
}

func main() {
	f, err := os.Open("input.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()

	rows := []string{}

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		rows = append(rows, scanner.Text())
	}
	if err := scanner.Err(); err != nil {
		log.Fatal(err)
	}

	antennas := map[rune]map[position]bool{}
	for row, line := range rows {
		for col, c := range line {
			if c == '.' {
				continue
			}
			a, ok := antennas[c]
			if !ok {
				a = map[position]bool{}
			}
			a[position{row: row, col: col}] = true
			antennas[c] = a
		}
	}

	antinodes := map[position]bool{}
	for row, line := range rows {
		for col := range line {
			pos := position{row: row, col: col}
			for _, ants := range antennas {
				for a := range ants {
					if a == pos {
						continue
					}
					v := &vector{point: pos, rd: a.row - pos.row, cd: a.col - pos.col}
					d := v.double().end()
					if _, ok := ants[d]; ok {
						antinodes[pos] = true
					}
				}
			}
		}
	}

	print(rows, antinodes)
	fmt.Printf("Part 1: %d\n", len(antinodes))

	antinodes = map[position]bool{}
	for row, line := range rows {
		for col := range line {
			pos := position{row: row, col: col}
			for _, ants := range antennas {
				// All antennas are in line with themselves and one other antenna.
				if _, ok := ants[pos]; ok && len(ants) > 1 {
					antinodes[pos] = true
					continue
				}

				slopes := map[float64]bool{}
				for a := range ants {
					v := &vector{point: pos, rd: a.row - pos.row, cd: a.col - pos.col}
					slope := v.slope()
					if _, ok := slopes[slope]; ok {
						antinodes[pos] = true
					}
					slopes[slope] = true
				}
			}
		}
	}

	print(rows, antinodes)
	fmt.Printf("Part 2: %d\n", len(antinodes))
}
