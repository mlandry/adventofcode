package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"regexp"
	"strconv"
	"strings"
)

type position struct {
	x int
	y int
}

type robot struct {
	x    int
	y    int
	velx int
	vely int
}

var robotRegexp = regexp.MustCompile(`p=(\d+),(\d+) v=(-?\d+),(-?\d+)`)

func mustAtoi(s string) int {
	i, err := strconv.Atoi(s)
	if err != nil {
		log.Fatal(err)
	}
	return i
}

func parse(line string) *robot {
	m := robotRegexp.FindStringSubmatch(line)
	if len(m) == 0 {
		log.Fatalf("failed to parse robot: %s", line)
	}
	return &robot{
		x:    mustAtoi(m[1]),
		y:    mustAtoi(m[2]),
		velx: mustAtoi(m[3]),
		vely: mustAtoi(m[4]),
	}
}

type area struct {
	width  int
	height int
	robots []*robot
}

func (a *area) xmod(x int) int {
	if x >= 0 {
		return x % a.width
	} else {
		return (a.width + x) % a.width
	}
}

func (a *area) ymod(y int) int {
	if y >= 0 {
		return y % a.height
	} else {
		return (a.height + y) % a.height
	}
}

func (a *area) tick() {
	for _, r := range a.robots {
		r.x = a.xmod(r.x + r.velx)
		r.y = a.ymod(r.y + r.vely)
	}
}

func (a *area) countMap() map[position]int {
	count := map[position]int{}
	for _, r := range a.robots {
		p := position{r.x, r.y}
		c := count[p]
		count[p] = c + 1
	}
	return count
}

func (a *area) inBounds(pos position) bool {
	return pos.x >= 0 && pos.x < a.width && pos.y >= 0 && pos.y < a.height
}

func (a *area) findClumb(pos position, counts map[position]int) map[position]bool {
	visited := map[position]bool{}
	visited[pos] = true
	q := make(chan position, a.height*a.width)
	q <- pos
	for {
		select {
		case p := <-q:
			for _, next := range []position{
				{p.x, p.y - 1},
				{p.x, p.y + 1},
				{p.x - 1, p.y},
				{p.x + 1, p.y},
			} {
				if !a.inBounds(next) {
					continue
				}
				if _, ok := visited[next]; ok {
					continue
				}
				if _, ok := counts[next]; !ok {
					continue
				}
				visited[next] = true
				q <- next
			}
		default:
			return visited
		}
	}
}

func (a *area) countClumped() int {
	counts := a.countMap()
	visited := map[position]bool{}
	max := 0
	for p := range counts {
		if _, ok := visited[p]; ok {
			continue
		}
		clump := a.findClumb(p, counts)
		if l := len(clump); l > max {
			max = l
		}
		for c := range clump {
			visited[c] = true
		}
	}
	return max
}

func (a *area) String() string {
	count := a.countMap()

	sb := strings.Builder{}
	for y := 0; y < a.height; y++ {
		for x := 0; x < a.width; x++ {
			if c, ok := count[position{x, y}]; ok {
				sb.WriteString(strconv.Itoa(c))
			} else {
				sb.WriteRune('.')
			}
		}
		sb.WriteRune('\n')
	}
	return sb.String()
}

type quadrant struct {
	x int
	y int
	w int
	h int
}

func (q *quadrant) contains(r *robot) bool {
	return r.x >= q.x && r.x < (q.x+q.w) && r.y >= q.y && r.y < (q.y+q.h)
}

func (a *area) safetyFactor() int64 {
	qw := a.width / 2
	qh := a.height / 2
	quads := []quadrant{
		{x: 0, y: 0, w: qw, h: qh},
		{x: qw + 1, y: 0, w: qw, h: qh},
		{x: 0, y: qh + 1, w: qw, h: qh},
		{x: qw + 1, y: qh + 1, w: qw, h: qh},
	}
	result := int64(1)
	for _, q := range quads {
		s := int64(0)
		for _, r := range a.robots {
			if q.contains(r) {
				s += 1
			}
		}
		result *= s
	}
	return result
}

type params struct {
	f string
	w int
	h int
}

var example = params{"example.txt", 11, 7}
var input = params{"input.txt", 101, 103}

func main() {
	param := input

	file, err := os.Open(param.f)
	if err != nil {
		log.Fatal(err)
	}
	defer file.Close()

	var lines []string
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		lines = append(lines, scanner.Text())
	}
	if err := scanner.Err(); err != nil {
		log.Fatal(err)
	}

	a := &area{width: param.w, height: param.h}
	for _, line := range lines {
		a.robots = append(a.robots, parse(line))
	}
	for i := 0; i < 100; i++ {
		a.tick()
	}
	fmt.Printf("Part 1: %d\n", a.safetyFactor())

	a.robots = nil
	for _, line := range lines {
		a.robots = append(a.robots, parse(line))
	}
	for i := 0; i < 10000; i++ {
		a.tick()
		clumped := a.countClumped()
		// Threshold picked from manual observation!
		if clumped > 100 {
			fmt.Printf("Part 2? Seconds: %d, Clumped: %d\n", (i + 1), clumped)
			fmt.Println(a.String())
			break
		}
	}
}
