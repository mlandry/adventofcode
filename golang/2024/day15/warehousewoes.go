package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"slices"
	"strings"
)

type position struct {
	row int
	col int
}

func (p position) gps() int {
	return 100*p.row + p.col
}

func (p position) next(dir rune) position {
	switch dir {
	case 'v':
		return position{p.row + 1, p.col}
	case '>':
		return position{p.row, p.col + 1}
	case '<':
		return position{p.row, p.col - 1}
	case '^':
		return position{p.row - 1, p.col}
	default:
		panic("unrecognized direction")
	}
}

type warehouse struct {
	rows [][]byte
}

func (w *warehouse) findRobot() position {
	for r, row := range w.rows {
		for c, v := range row {
			if v == '@' {
				return position{r, c}
			}
		}
	}
	panic("couldn't find robot!")
}

func (w *warehouse) pushableBoxes(robot position, dir rune) []position {
	next := robot
	var boxes []position
	for {
		next = next.next(dir)
		switch w.rows[next.row][next.col] {
		case '#':
			return nil
		case '.':
			return boxes
		case 'O':
			boxes = append(boxes, next)
		default:
			panic("unexpected char")
		}
	}
}

func (w *warehouse) move(robot position, dir rune) position {
	boxes := w.pushableBoxes(robot, dir)
	if len(boxes) > 0 {
		slices.Reverse(boxes)
		for _, b := range boxes {
			next := b.next(dir)
			w.rows[next.row][next.col] = 'O'
		}
		first := boxes[len(boxes)-1]
		w.rows[first.row][first.col] = '@'
		w.rows[robot.row][robot.col] = '.'
		return first
	}
	if next := robot.next(dir); w.rows[next.row][next.col] == '.' {
		w.rows[next.row][next.col] = '@'
		w.rows[robot.row][robot.col] = '.'
		return next
	}
	return robot
}

func (w *warehouse) pushableBoxRows(robot position, dir rune) [][]position {
	curr := []position{robot}
	var boxRows [][]position
	for {
		var row []position
		clear := true

		idempotentAppend := func(p position) {
			// Inefficiently search through the array to make sure we don't already have this box part.
			if !slices.Contains(row, p) {
				row = append(row, p)
			}
		}

		for _, c := range curr {
			next := c.next(dir)
			switch w.rows[next.row][next.col] {
			case '#':
				return nil
			case '.':
				// No-op
			case '[':
				idempotentAppend(next)
				if dir == '^' || dir == 'v' {
					// Push both this spot and the other half of the box to the right.
					idempotentAppend(next.next('>'))
				}

				clear = false
			case ']':
				if dir == '^' || dir == 'v' {
					// Push both this spot and the other half of the box to the left.
					idempotentAppend(next.next('<'))
				}
				idempotentAppend(next)
				clear = false
			default:
				panic("unexpected char")
			}
		}
		if clear {
			return boxRows
		}
		boxRows = append(boxRows, row)
		curr = row
	}
}

func (w *warehouse) moveLarge(robot position, dir rune) position {
	boxRows := w.pushableBoxRows(robot, dir)
	if len(boxRows) > 0 {
		slices.Reverse(boxRows)
		for _, row := range boxRows {
			for _, b := range row {
				next := b.next(dir)
				w.rows[next.row][next.col] = w.rows[b.row][b.col]
				w.rows[b.row][b.col] = '.'
			}
		}
		next := robot.next(dir)
		w.rows[next.row][next.col] = '@'
		w.rows[robot.row][robot.col] = '.'
		return next
	}
	if next := robot.next(dir); w.rows[next.row][next.col] == '.' {
		w.rows[next.row][next.col] = '@'
		w.rows[robot.row][robot.col] = '.'
		return next
	}
	return robot
}

func (w *warehouse) sumBoxGps() int64 {
	sum := int64(0)
	for r, row := range w.rows {
		for c, v := range row {
			if v != 'O' && v != '[' {
				continue
			}
			p := position{r, c}
			sum += int64(p.gps())
		}
	}
	return sum
}

func (w *warehouse) String() string {
	sb := strings.Builder{}
	for _, row := range w.rows {
		sb.Write(row)
		sb.WriteRune('\n')
	}
	return sb.String()
}

func main() {
	file, err := os.Open("input.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer file.Close()

	var rows []string
	var dirs string
	var d bool
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()
		if d {
			dirs += strings.TrimSpace(line)
		} else if line == "" {
			d = true
		} else {
			rows = append(rows, line)
		}
	}
	if err := scanner.Err(); err != nil {
		log.Fatal(err)
	}

	w := warehouse{}
	for _, row := range rows {
		w.rows = append(w.rows, []byte(row))
	}

	// fmt.Println("Initial state:")
	// fmt.Println(w.String())

	robot := w.findRobot()
	for _, d := range dirs {
		// fmt.Printf("Move %s:\n", string(d))
		robot = w.move(robot, d)
		// fmt.Println(w.String())
	}
	fmt.Printf("Part 1: %d\n", w.sumBoxGps())

	// Part 2 with double width boxes and walls.
	w = warehouse{}
	for _, row := range rows {
		var wr []byte
		for _, b := range row {
			switch b {
			case '#':
				wr = append(wr, '#', '#')
			case 'O':
				wr = append(wr, '[', ']')
			case '.':
				wr = append(wr, '.', '.')
			case '@':
				wr = append(wr, '@', '.')
			}
		}
		w.rows = append(w.rows, wr)
	}

	// fmt.Println("Initial state:")
	// fmt.Println(w.String())

	robot = w.findRobot()
	for _, d := range dirs {
		// fmt.Printf("Move %s:\n", string(d))
		robot = w.moveLarge(robot, d)
		// fmt.Println(w.String())
	}
	fmt.Printf("Part 2: %d\n", w.sumBoxGps())
}
