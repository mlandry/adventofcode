package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
)

const xmas = "XMAS"

func main() {
	f, err := os.Open("input.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()

	lines := []string{}

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		lines = append(lines, scanner.Text())
	}

	if err := scanner.Err(); err != nil {
		log.Fatal(err)
	}

	checkForXmas := func(startRow int, startCol int, rd int, cd int) bool {
		for i := 0; i < len(xmas); i++ {
			x := xmas[i]
			row := startRow + (rd * i)
			col := startCol + (cd * i)
			if row < 0 || row >= len(lines) || col < 0 || col >= len(lines[row]) || x != lines[row][col] {
				return false
			}
		}
		return true
	}

	checkForCross := func(row int, col int) bool {
		if row <= 0 || row >= (len(lines)-1) || col <= 0 || col >= (len(lines[row])-1) || lines[row][col] != 'A' {
			return false
		}
		topLeft := lines[row-1][col-1]
		bottomRight := lines[row+1][col+1]
		topRight := lines[row-1][col+1]
		bottomLeft := lines[row+1][col-1]
		for _, b := range []byte{topLeft, bottomRight, topRight, bottomLeft} {
			if b != 'M' && b != 'S' {
				return false
			}
		}
		if topLeft == bottomRight {
			return false
		}
		if topRight == bottomLeft {
			return false
		}
		return true
	}

	count := 0
	for row, line := range lines {
		for col, r := range line {
			if r != 'X' {
				continue
			}
			for rd := -1; rd <= 1; rd++ {
				for cd := -1; cd <= 1; cd++ {
					if rd == 0 && cd == 0 {
						continue
					}
					if checkForXmas(row, col, rd, cd) {
						count += 1
					}
				}
			}
		}
	}
	fmt.Printf("Part 1: %d\n", count)

	count = 0
	for row, line := range lines {
		for col, r := range line {
			if r != 'A' {
				continue
			}
			if checkForCross(row, col) {
				count += 1
			}
		}
	}
	fmt.Printf("Part 2: %d\n", count)
}
