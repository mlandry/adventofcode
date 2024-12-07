package main

import (
	"bufio"
	"errors"
	"fmt"
	"log"
	"os"
)

type position struct {
	row int
	col int
}

type maze struct {
	rows []string
}

func (m *maze) findGuard() (position, rune, error) {
	for r, row := range m.rows {
		for c, v := range row {
			if v == '^' || v == '<' || v == '>' || v == 'v' {
				return position{row: r, col: c}, v, nil
			}
		}
	}
	return position{row: 0, col: 0}, 0, errors.New("guard not found")
}

func (m *maze) navigate(pos position, curr rune, obs *position) (int, bool, error) {
	visited := map[position]map[rune]bool{}

	// Returns true if we've already visited this position facing the same way.
	markVisited := func(p position, c rune) bool {
		m, ok := visited[p]
		if !ok {
			m = map[rune]bool{}
			visited[p] = m
		}
		if v, ok := m[c]; v && ok {
			return true
		}
		m[c] = true
		return false
	}

	isObstruction := func(p position) bool {
		return m.rows[p.row][p.col] == '#' || (obs != nil && *obs == p)
	}

	for {
		loop := markVisited(pos, curr)
		if loop {
			return 0, true, nil
		}
		switch curr {
		case '^':
			if pos.row == 0 {
				return len(visited), false, nil
			}
			next := position{row: pos.row - 1, col: pos.col}
			if isObstruction(next) {
				curr = '>'
			} else {
				pos = next
			}
		case '>':
			if pos.col == (len(m.rows[pos.row]) - 1) {
				return len(visited), false, nil
			}
			next := position{row: pos.row, col: pos.col + 1}
			if isObstruction(next) {
				curr = 'v'
			} else {
				pos = next
			}
		case 'v':
			if pos.row == (len(m.rows) - 1) {
				return len(visited), false, nil
			}
			next := position{row: pos.row + 1, col: pos.col}
			if isObstruction(next) {
				curr = '<'
			} else {
				pos = next
			}
		case '<':
			if pos.col == 0 {
				return len(visited), false, nil
			}
			next := position{row: pos.row, col: pos.col - 1}
			if isObstruction(next) {
				curr = '^'
			} else {
				pos = next
			}
		default:
			return 0, false, fmt.Errorf("unexpected character: %s", string(curr))
		}
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

	maze := &maze{rows: rows}

	pos, curr, err := maze.findGuard()
	if err != nil {
		log.Fatal(err)
	}

	visited, _, err := maze.navigate(pos, curr, nil)
	if err != nil {
		log.Fatal(err)
	}

	fmt.Printf("Part 1: %d\n", visited)

	loopObstructions := 0
	for row := range rows {
		for col, v := range rows[row] {
			if v != '.' {
				continue
			}
			_, loop, err := maze.navigate(pos, curr, &position{row: row, col: col})
			if err != nil {
				log.Fatal(err)
			}
			if loop {
				loopObstructions += 1
			}
		}
	}

	fmt.Printf("Part 2: %d\n", loopObstructions)
}
