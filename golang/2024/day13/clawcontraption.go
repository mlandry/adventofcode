package main

import (
	"bufio"
	"fmt"
	"log"
	"math"
	"os"
	"regexp"
	"strconv"
)

var buttonRegexp = regexp.MustCompile(`Button (\w+): X\+(\d+), Y\+(\d+)`)
var prizeRegexp = regexp.MustCompile(`Prize: X=(\d+), Y=(\d+)`)

type parse int

const (
	a parse = iota
	b
	prize
	blank
)

type button struct {
	x int64
	y int64
}

type position struct {
	x int64
	y int64
}

type clawMachine struct {
	a     button
	b     button
	prize position
}

func parseButton(line string, letter string) button {
	m := buttonRegexp.FindStringSubmatch(line)
	if len(m) == 0 || m[1] != letter {
		log.Fatalf("parse error on button: %s", line)
	}
	x, err := strconv.Atoi(m[2])
	if err != nil {
		log.Fatal(err)
	}
	y, err := strconv.Atoi(m[3])
	if err != nil {
		log.Fatal(err)
	}
	return button{int64(x), int64(y)}
}

func parsePrize(line string) position {
	m := prizeRegexp.FindStringSubmatch(line)
	if len(m) == 0 {
		log.Fatalf("parse error on prize: %s", line)
	}
	x, err := strconv.Atoi(m[1])
	if err != nil {
		log.Fatal(err)
	}
	y, err := strconv.Atoi(m[2])
	if err != nil {
		log.Fatal(err)
	}
	return position{int64(x), int64(y)}
}

type presses struct {
	a int64
	b int64
}

func (p *presses) cost() int64 {
	return (3 * p.a) + p.b
}

type player struct {
	cm    *clawMachine
	cache map[position]*presses
}

func (p *player) findCheapest(prize position) *presses {
	if presses, ok := p.cache[prize]; ok {
		return presses
	}
	if prize.x == 0 && prize.y == 0 {
		return &presses{0, 0}
	}

	var cheapest *presses

	ap := position{x: prize.x - p.cm.a.x, y: prize.y - p.cm.a.y}
	if ap.x >= 0 && ap.y >= 0 {
		ac := p.findCheapest(ap)
		if ac != nil {
			cheapest = &presses{a: ac.a + 1, b: ac.b}
		}
	}

	bp := position{x: prize.x - p.cm.b.x, y: prize.y - p.cm.b.y}
	if bp.x >= 0 && bp.y >= 0 {
		bc := p.findCheapest(bp)
		if bc != nil {
			pr := &presses{a: bc.a, b: bc.b + 1}
			if cheapest == nil || pr.cost() < cheapest.cost() {
				cheapest = pr
			}
		}
	}

	p.cache[prize] = cheapest
	return cheapest
}

func solve(cm *clawMachine) int64 {
	// Button A: X+94, Y+34
	// Button B: X+22, Y+67
	// Prize: X=8400, Y=5400

	// A_x*a + B_x*b = P_x
	// A_y*a + B_y*b = P_y

	// Multiply both eq1 by A_y and eq2 by A_x
	// A_x*A_y*a + A_y*B_x*b = A_y*P_x
	// A_x*A_y*a + A_x*B_y*b = A_x*P_y

	// Subtract the equations to solve for b
	// (A_y*B_x - A_x*B_y)*b = A_y*P_x - A_x*P_y
	// b = (A_y*P_x - A_x*P_y) / (A_y*B_x - A_x*B_y)
	b := float64(cm.a.y*cm.prize.x-cm.a.x*cm.prize.y) / float64(cm.a.y*cm.b.x-cm.a.x*cm.b.y)
	if b != math.Trunc(b) {
		return -1
	}

	// a can then be solved from b
	// a = (P_x - B_x*b)/A_x
	a := float64(cm.prize.x-cm.b.x*int64(b)) / float64(cm.a.x)
	if a != math.Trunc(a) {
		return -1
	}
	return 3*int64(a) + int64(b)
}

func main() {
	file, err := os.Open("input.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer file.Close()

	machines := []*clawMachine{}

	next := a
	curr := &clawMachine{}
	machines = append(machines, curr)

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()
		switch next {
		case a:
			curr.a = parseButton(line, "A")
			next = b
		case b:
			curr.b = parseButton(line, "B")
			next = prize
		case prize:
			curr.prize = parsePrize(line)
			next = blank
		case blank:
			curr = &clawMachine{}
			machines = append(machines, curr)
			next = a
		}
	}
	if err := scanner.Err(); err != nil {
		log.Fatal(err)
	}

	tokens := int64(0)
	for _, cm := range machines {
		p := &player{cm: cm, cache: map[position]*presses{}}
		presses := p.findCheapest(cm.prize)
		if presses != nil {
			tokens += presses.cost()
		}
	}
	fmt.Printf("Part 1: %d\n", tokens)

	tokens = 0
	for _, cm := range machines {
		cm.prize.x += 10000000000000
		cm.prize.y += 10000000000000
		t := solve(cm)
		if t > 0 {
			tokens += t
		}
	}
	fmt.Printf("Part 2: %d\n", tokens)
}
