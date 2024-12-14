package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
)

type dir int

const (
	up dir = iota
	down
	left
	right
)

type position struct {
	row int
	col int
}

func (p position) move(d dir) position {
	switch d {
	case up:
		return position{row: p.row - 1, col: p.col}
	case down:
		return position{row: p.row + 1, col: p.col}
	case left:
		return position{row: p.row, col: p.col - 1}
	case right:
		return position{row: p.row, col: p.col + 1}
	}
	panic("invalid dir " + string(d))
}

type fence struct {
	plant     byte
	area      int
	perimeter int
}

func (f *fence) cost() int64 {
	return int64(f.area) * int64(f.perimeter)
}

func (f *fence) String() string {
	return fmt.Sprintf("{plant:%s area:%d perimter:%d}", string(f.plant), f.area, f.perimeter)
}

type fencer struct {
	garden []string
	fenced map[position]bool
}

func (f *fencer) inBounds(p position) bool {
	return p.row >= 0 && p.row < len(f.garden) && p.col >= 0 && p.col < len(f.garden[p.row])
}

// Strategy function for counting perimeter cost of a fence.
type perimeterStrategy = func(edges map[position]map[dir]bool, pos position, d dir) bool

// Simple strategy - every edge space counts for 1.
var countPerimeter = func(edges map[position]map[dir]bool, pos position, d dir) bool {
	return true
}

// Strategy where each "side" (adjacent squares with aligned edges) counts for 1.
// We check if either of our neighbours has already counted an edge in this direction
// and only increment if not.
var countSides = func(edges map[position]map[dir]bool, pos position, d dir) bool {
	switch d {
	case up:
		fallthrough
	case down:
		if left, ok := edges[position{row: pos.row, col: pos.col - 1}]; ok && left[d] {
			return false
		}
		if right, ok := edges[position{row: pos.row, col: pos.col + 1}]; ok && right[d] {
			return false
		}
		return true
	case left:
		fallthrough
	case right:
		if up, ok := edges[position{row: pos.row - 1, col: pos.col}]; ok && up[d] {
			return false
		}
		if down, ok := edges[position{row: pos.row + 1, col: pos.col}]; ok && down[d] {
			return false
		}
		return true
	}
	panic("invalid dir " + string(d))
}

// Fence a plot of one kind of plant, starting at start position.
// Perimeter is counted according to the strategy function.
func (f *fencer) fencePlot(start position, shouldIncrementPerimter perimeterStrategy) *fence {
	fnc := &fence{plant: f.garden[start.row][start.col], area: 1}
	edges := map[position]map[dir]bool{}

	visited := map[position]bool{start: true}
	q := make(chan position, len(f.garden)*len(f.garden[0]))

	addEdge := func(p position, d dir) {
		dEdges, ok := edges[p]
		if !ok {
			dEdges = map[dir]bool{}
			edges[p] = dEdges
		}
		dEdges[d] = true
	}

	q <- start
	for {
		select {
		case p := <-q:
			f.fenced[p] = true
			for _, d := range []dir{up, down, left, right} {
				next := p.move(d)
				if !f.inBounds(next) {
					addEdge(p, d)
					if shouldIncrementPerimter(edges, p, d) {
						fnc.perimeter += 1
					}
					continue
				}
				if _, ok := visited[next]; ok {
					continue
				}
				plant := f.garden[next.row][next.col]
				if plant == fnc.plant {
					visited[next] = true
					fnc.area += 1
					q <- next
				} else {
					addEdge(p, d)
					if shouldIncrementPerimter(edges, p, d) {
						fnc.perimeter += 1
					}
				}
			}
		default:
			return fnc
		}
	}
}

func (f *fencer) measure(strategy perimeterStrategy) int64 {
	cost := int64(0)
	for r, row := range f.garden {
		for c := range row {
			p := position{row: r, col: c}
			if _, ok := f.fenced[p]; ok {
				continue
			}
			fen := f.fencePlot(p, strategy)
			cost += fen.cost()
		}
	}
	return cost
}

func main() {
	file, err := os.Open("input.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer file.Close()

	rows := []string{}

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		rows = append(rows, scanner.Text())
	}
	if err := scanner.Err(); err != nil {
		log.Fatal(err)
	}

	f := fencer{garden: rows, fenced: map[position]bool{}}
	cost := f.measure(countPerimeter)
	fmt.Printf("Part 1: %d\n", cost)

	f = fencer{garden: rows, fenced: map[position]bool{}}
	cost = f.measure(countSides)
	fmt.Printf("Part 2: %d\n", cost)
}
