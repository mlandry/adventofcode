package main

import (
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"
)

func mustParse(s string) stone {
	i, err := strconv.ParseInt(s, 10, 64)
	if err != nil {
		panic(err)
	}
	return stone(i)
}

type stone int64

func (s stone) str() string {
	return strconv.FormatInt(int64(s), 10)
}

func (s stone) change() []stone {
	if s == 0 {
		return []stone{1}
	}
	str := s.str()
	d := len(str)
	if d%2 == 0 {
		return []stone{
			mustParse(str[:(d / 2)]),
			mustParse(str[(d / 2):]),
		}
	}
	return []stone{s * 2024}
}

type input struct {
	stones  stone
	changes int
}

type simulator struct {
	cache map[input]int64
}

func (s simulator) simulate(stones []stone, changes int) int64 {
	result := int64(0)
	for _, st := range stones {
		result += s.count(st, changes)
	}
	return result
}

func (s simulator) count(stone stone, changes int) int64 {
	if changes == 0 {
		return 1
	}
	in := input{stone, changes}
	if v, ok := s.cache[in]; ok {
		return v
	}
	result := int64(0)
	for _, st := range stone.change() {
		result += s.count(st, changes-1)
	}
	s.cache[in] = result
	return result
}

func main() {
	f, err := os.ReadFile("input.txt")
	if err != nil {
		log.Fatal(err)
	}

	s := strings.TrimSpace(string(f))
	stones := []stone{}
	for _, sp := range strings.Split(s, " ") {
		stones = append(stones, mustParse(sp))
	}

	sim := simulator{cache: map[input]int64{}}
	fmt.Printf("Part 1: %d\n", sim.simulate(stones, 25))

	sim = simulator{cache: map[input]int64{}}
	fmt.Printf("Part 2: %d\n", sim.simulate(stones, 75))
}
