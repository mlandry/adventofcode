package main

import (
	"bufio"
	"fmt"
	"os"
)

func abs(v int) int {
	if v >= 0 {
		return v
	}
	return -v
}

type position struct {
	row int
	col int
}

func (p position) distance(o position) int {
	return abs(o.row-p.row) + abs(o.col-p.col)
}

type racetrack struct {
	rows []string
}

func (r *racetrack) inBounds(p position) bool {
	return p.row >= 0 && p.row < len(r.rows) && p.col >= 0 && p.col < len(r.rows[p.row])
}

func (r *racetrack) findStart() position {
	for r, row := range r.rows {
		for c, val := range row {
			if val == 'S' {
				return position{r, c}
			}
		}
	}
	panic("couldn't find start")
}

type state struct {
	curr    position
	path    []position
	visited map[position]bool
}

func (st *state) append(pos position) *state {
	copy := &state{
		curr:    pos,
		path:    make([]position, 0, len(st.path)+1),
		visited: make(map[position]bool, len(st.path)+1),
	}
	for _, p := range st.path {
		copy.path = append(copy.path, p)
		copy.visited[p] = true
	}
	copy.path = append(copy.path, pos)
	copy.visited[pos] = true
	return copy
}

func (r *racetrack) findPath() []position {
	q := make(chan *state, len(r.rows)*len(r.rows[0]))
	start := r.findStart()
	q <- &state{
		curr:    start,
		path:    []position{start},
		visited: map[position]bool{start: true},
	}
	for {
		select {
		case st := <-q:
			for _, pos := range []position{
				{st.curr.row + 1, st.curr.col},
				{st.curr.row - 1, st.curr.col},
				{st.curr.row, st.curr.col + 1},
				{st.curr.row, st.curr.col - 1},
			} {
				if !r.inBounds(pos) || st.visited[pos] {
					continue
				}
				val := r.rows[pos.row][pos.col]
				if val == '#' {
					continue
				}
				next := st.append(pos)
				if val == 'E' {
					return next.path
				}
				q <- next
			}
		default:
			panic("path not found")
		}
	}
}

type cheat struct {
	start position
	end   position
}

func (r *racetrack) findCheats(path []position, max int) map[cheat]int {
	cheats := map[cheat]int{}
	for i, start := range path {
		for j := i + 1; j < len(path); j++ {
			end := path[j]
			if dis := start.distance(end); dis <= max {
				if savings := j - i - dis; savings > 0 {
					cheats[cheat{start, end}] = savings
				}
			}
		}
	}
	return cheats
}

func main() {
	file, err := os.Open("input.txt")
	if err != nil {
		panic(err)
	}

	track := &racetrack{}

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		track.rows = append(track.rows, scanner.Text())
	}

	path := track.findPath()

	cheats := track.findCheats(path, 2)
	threshold := 100
	count := 0
	for _, savings := range cheats {
		if savings >= threshold {
			count++
		}
	}
	fmt.Printf("Part 1: %d\n", count)

	cheats = track.findCheats(path, 20)
	count = 0
	for _, savings := range cheats {
		if savings >= threshold {
			count++
		}
	}
	fmt.Printf("Part 2: %d\n", count)
}
