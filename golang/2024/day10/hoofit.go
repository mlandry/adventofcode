package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
)

type position struct {
	row int
	col int
}

type trailMap struct {
	rows [][]int8
}

func newTrailMap() *trailMap {
	return &trailMap{rows: [][]int8{}}
}

func (tm *trailMap) parseRow(line string) {
	row := make([]int8, len(line))
	for i, r := range line {
		row[i] = int8(r - '0')
	}
	tm.rows = append(tm.rows, row)
}

func (tm *trailMap) inBounds(pos position) bool {
	return pos.row >= 0 && pos.row < len(tm.rows) && pos.col >= 0 && pos.col < len(tm.rows[pos.row])
}

func (tm *trailMap) findTrailHeads() []position {
	heads := []position{}
	for r, row := range tm.rows {
		for c, v := range row {
			if v == 0 {
				heads = append(heads, position{row: r, col: c})
			}
		}
	}
	return heads
}

type hiker struct {
	tm      *trailMap
	start   position
	visited map[position]bool
}

func newHiker(tm *trailMap, start position) *hiker {
	if !tm.inBounds(start) || tm.rows[start.row][start.col] != 0 {
		log.Fatal("invalid hiker start")
	}
	return &hiker{tm: tm, start: start, visited: map[position]bool{}}
}

func (h *hiker) spawnHiker(start position) *hiker {
	visited := make(map[position]bool, len(h.visited))
	return &hiker{tm: h.tm, start: start, visited: visited}
}

func (h *hiker) score() int {
	q := make(chan position, len(h.tm.rows)*len(h.tm.rows[0]))
	trails := 0
	h.visited[h.start] = true
	q <- h.start
	for {
		select {
		case p := <-q:
			val := h.tm.rows[p.row][p.col]
			if val == 9 {
				trails += 1
				break
			}
			next := []position{
				{row: p.row - 1, col: p.col},
				{row: p.row + 1, col: p.col},
				{row: p.row, col: p.col - 1},
				{row: p.row, col: p.col + 1},
			}
			for _, n := range next {
				if !h.tm.inBounds(n) {
					continue
				}
				if _, ok := h.visited[n]; ok {
					continue
				}
				if h.tm.rows[n.row][n.col]-val != 1 {
					continue
				}
				h.visited[n] = true
				q <- n
			}
		default:
			return trails
		}
	}
}

func (h *hiker) rating() int {
	h.visited[h.start] = true
	val := h.tm.rows[h.start.row][h.start.col]
	if val == 9 {
		return 1
	}
	next := []position{
		{row: h.start.row - 1, col: h.start.col},
		{row: h.start.row + 1, col: h.start.col},
		{row: h.start.row, col: h.start.col - 1},
		{row: h.start.row, col: h.start.col + 1},
	}
	rating := 0
	for _, p := range next {
		if !h.tm.inBounds(p) {
			continue
		}
		if _, ok := h.visited[p]; ok {
			continue
		}
		if h.tm.rows[p.row][p.col]-val != 1 {
			continue
		}
		rating += h.spawnHiker(p).rating()
	}
	return rating
}

func main() {
	f, err := os.Open("input.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()

	tm := newTrailMap()

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		tm.parseRow(scanner.Text())
	}
	if err := scanner.Err(); err != nil {
		log.Fatal(err)
	}

	sum := 0
	for _, head := range tm.findTrailHeads() {
		sum += newHiker(tm, head).score()
	}
	fmt.Printf("Part 1: %d\n", sum)

	sum = 0
	for _, head := range tm.findTrailHeads() {
		sum += newHiker(tm, head).rating()
	}
	fmt.Printf("Part 2: %d\n", sum)
}
