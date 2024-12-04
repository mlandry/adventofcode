package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"
)

func isSafe(report []int, skip int) bool {
	var increasing bool
	prev := report[0]
	start := 1
	if skip == 0 {
		prev = report[1]
		start = 2
	}
	for i := start; i < len(report); i++ {
		if skip == i {
			continue
		}
		level := report[i]
		d := level - prev
		if d == 0 {
			return false
		} else if d > 0 {
			if i == start {
				increasing = true
			} else if !increasing {
				return false
			}
			if d > 3 {
				return false
			}
		} else {
			if i == start {
				increasing = false
			} else if increasing {
				return false
			}
			if d < -3 {
				return false
			}
		}
		prev = level
	}
	return true
}

func isSafeWithDampening(report []int) bool {
	for skip := -1; skip < len(report); skip++ {
		if isSafe(report, skip) {
			return true
		}
	}
	fmt.Printf("%+v\n", report)
	return false
}

func main() {
	f, err := os.Open("input.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()

	var reports [][]int
	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		sp := strings.Fields(scanner.Text())
		report := make([]int, len(sp))
		for i, s := range sp {
			report[i], err = strconv.Atoi(s)
			if err != nil {
				log.Fatal(err)
			}
		}
		reports = append(reports, report)
	}

	// Part 1.
	safeReports := 0
	for _, report := range reports {
		if isSafe(report, -1) {
			safeReports += 1
		}
	}
	log.Println("Part 1: " + strconv.Itoa(safeReports))

	// Part 2.
	safeReports = 0
	for _, report := range reports {
		if isSafeWithDampening(report) {
			safeReports += 1
		}
	}
	log.Println("Part 2: " + strconv.Itoa(safeReports))
}
