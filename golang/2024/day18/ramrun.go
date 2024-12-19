package main

import (
	"bufio"
	"fmt"
	"os"
	"strconv"
	"strings"
)

type position struct {
	x, y int
}

func parse(line string) position {
	sp := strings.Split(line, ",")
	if len(sp) != 2 {
		panic("expected x,y")
	}
	x, err := strconv.Atoi(sp[0])
	if err != nil {
		panic(err)
	}
	y, err := strconv.Atoi(sp[1])
	if err != nil {
		panic(err)
	}
	return position{x, y}
}

type space struct {
	w, h      int
	corrupted map[position]bool
}

type state struct {
	pos   position
	steps int
}

func (sp *space) String() string {
	sb := strings.Builder{}
	for y := 0; y < sp.h; y++ {
		for x := 0; x < sp.w; x++ {
			if sp.isCorrupted(position{x, y}) {
				sb.WriteRune('#')
			} else {
				sb.WriteRune('.')
			}
		}
		sb.WriteRune('\n')
	}
	return sb.String()
}

func (sp *space) inBounds(p position) bool {
	return p.x >= 0 && p.x < sp.w && p.y >= 0 && p.y < sp.h
}

func (sp *space) isCorrupted(p position) bool {
	_, ok := sp.corrupted[p]
	return ok
}

func (sp *space) findShortestPath() int {
	start := position{0, 0}
	goal := position{sp.w - 1, sp.h - 1}

	visited := map[position]bool{start: true}

	q := make(chan state, sp.w*sp.h)
	q <- state{start, 0}

	for {
		select {
		case st := <-q:
			if st.pos == goal {
				return st.steps
			}
			for _, next := range []position{
				{st.pos.x + 1, st.pos.y},
				{st.pos.x, st.pos.y + 1},
				{st.pos.x - 1, st.pos.y},
				{st.pos.x, st.pos.y - 1},
			} {
				if !sp.inBounds(next) || sp.isCorrupted(next) {
					continue
				}
				if _, ok := visited[next]; ok {
					continue
				}
				visited[next] = true
				q <- state{next, st.steps + 1}
			}
		default:
			return -1
		}
	}
}

type params struct {
	f       string
	w, h, b int
}

var example = params{f: "example.txt", w: 7, h: 7, b: 12}
var input = params{f: "input.txt", w: 71, h: 71, b: 1024}

func main() {
	p := input
	f, err := os.Open(p.f)
	if err != nil {
		panic(err)
	}

	positions := []position{}

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		positions = append(positions, parse(scanner.Text()))
	}
	if err := scanner.Err(); err != nil {
		panic(err)
	}

	sp := &space{w: p.w, h: p.h, corrupted: map[position]bool{}}
	for b := 0; b < p.b; b++ {
		sp.corrupted[positions[b]] = true
	}

	fmt.Printf("Part 1: %d\n", sp.findShortestPath())

	for b := p.b; b < len(positions); b++ {
		sp.corrupted[positions[b]] = true
		if sp.findShortestPath() > 0 {
			continue
		}
		fmt.Printf("Part 2: %d,%d\n", positions[b].x, positions[b].y)
		break
	}
}
