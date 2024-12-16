package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"strings"
)

type coords struct {
	row int
	col int
}

type position struct {
	row int
	col int
	dir rune
}

func (p position) forward() position {
	switch p.dir {
	case 'v':
		return position{p.row + 1, p.col, p.dir}
	case '>':
		return position{p.row, p.col + 1, p.dir}
	case '<':
		return position{p.row, p.col - 1, p.dir}
	case '^':
		return position{p.row - 1, p.col, p.dir}
	default:
		panic("unrecognized direction")
	}
}

func (p position) turn(dir rune) position {
	return position{p.row, p.col, dir}
}

func (p position) turns() []position {
	switch p.dir {
	case 'v':
		fallthrough
	case '^':
		return []position{p.turn('<'), p.turn('>')}
	case '>':
		fallthrough
	case '<':
		return []position{p.turn('^'), p.turn('v')}
	default:
		panic("unrecognized direction")
	}
}

func (p position) coords() coords {
	return coords{p.row, p.col}
}

type maze struct {
	rows []string
}

func (m *maze) findStart() position {
	for r, row := range m.rows {
		for c, v := range row {
			if v == 'S' {
				return position{r, c, '>'}
			}
		}
	}
	panic("couldn't find start")
}

type state struct {
	pos   position
	score int64
	path  []coords
}

func copyAndAppend(path []coords, c coords) []coords {
	var copied []coords
	copied = append(copied, path...)
	copied = append(copied, c)
	return copied
}

func (m *maze) findLowestScore() (int64, map[coords]bool) {
	start := m.findStart()

	visited := map[position]int64{start: 0}

	q := make(chan state, len(m.rows)*len(m.rows[0]))
	q <- state{start, 0, []coords{start.coords()}}

	var lowest *int64
	bestPaths := map[coords]bool{}

	addPath := func(path []coords) {
		for _, p := range path {
			bestPaths[p] = true
		}
	}

	for {
		select {
		case p := <-q:
			// Consider moving forward.
			f := p.pos.forward()
			next := state{
				f,
				p.score + 1,
				copyAndAppend(p.path, f.coords()),
			}
			switch m.rows[next.pos.row][next.pos.col] {
			case '.', 'S':
				if prev, ok := visited[next.pos]; !ok || next.score <= prev {
					visited[next.pos] = next.score
					q <- next
				}
			case 'E':
				if lowest == nil || next.score < *lowest {
					lowest = &next.score
					bestPaths = map[coords]bool{}
					addPath(next.path)
				} else if next.score == *lowest {
					addPath(next.path)
				}
			}

			// Consider turning.
			for _, turn := range p.pos.turns() {
				next := state{
					turn,
					p.score + 1000,
					copyAndAppend(p.path, turn.coords()),
				}
				switch m.rows[next.pos.row][next.pos.col] {
				case '.', 'S':
					if prev, ok := visited[next.pos]; !ok || next.score <= prev {
						visited[next.pos] = next.score
						q <- next
					}
				case 'E':
					if lowest == nil || next.score < *lowest {
						lowest = &next.score
						bestPaths = map[coords]bool{}
						addPath(next.path)
					} else if next.score == *lowest {
						addPath(next.path)
					}
				}
			}
		default:
			return *lowest, bestPaths
		}
	}
}

func (m *maze) Print(pos position) {
	sb := strings.Builder{}
	for r, row := range m.rows {
		for c, v := range row {
			if r == pos.row && c == pos.col {
				sb.WriteRune(pos.dir)
			} else {
				sb.WriteRune(v)
			}
		}
		sb.WriteByte('\n')
	}
	fmt.Println(sb.String())
}

func (m *maze) PrintWithPath(path map[coords]bool) {
	sb := strings.Builder{}
	for r, row := range m.rows {
		for c, v := range row {
			p := coords{r, c}
			if _, ok := path[p]; ok {
				sb.WriteRune('0')
			} else {
				sb.WriteRune(v)
			}
		}
		sb.WriteByte('\n')
	}
	fmt.Println(sb.String())
}

func main() {
	file, err := os.Open("input.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer file.Close()

	var rows []string
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		rows = append(rows, scanner.Text())
	}
	if err := scanner.Err(); err != nil {
		log.Fatal(err)
	}

	m := &maze{rows}
	lowest, path := m.findLowestScore()
	fmt.Printf("Part 1: %d\n", lowest)
	fmt.Printf("Part 2: %d\n", len(path))
}
