package main

import (
	"bufio"
	"errors"
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"
)

type equation struct {
	test    int64
	numbers []int64
}

func (eq *equation) valid() bool {
	if len(eq.numbers) == 1 {
		return eq.test == eq.numbers[0]
	}
	idx := len(eq.numbers) - 1
	// Test addition.
	add := &equation{test: eq.test - eq.numbers[idx], numbers: eq.numbers[:idx]}
	if add.valid() {
		return true
	}
	// Test multiplication.
	if eq.test%eq.numbers[idx] != 0 {
		return false
	}
	mult := &equation{test: eq.test / eq.numbers[idx], numbers: eq.numbers[:idx]}
	return mult.valid()
}

func (eq *equation) validWithConcatenation() bool {
	if len(eq.numbers) == 1 {
		return eq.test == eq.numbers[0]
	}
	idx := len(eq.numbers) - 1
	// Test addition.
	add := &equation{test: eq.test - eq.numbers[idx], numbers: eq.numbers[:idx]}
	if add.validWithConcatenation() {
		return true
	}
	// Test multiplication.
	if eq.test%eq.numbers[idx] == 0 {
		mult := &equation{test: eq.test / eq.numbers[idx], numbers: eq.numbers[:idx]}
		if mult.validWithConcatenation() {
			return true
		}
	}
	// Test concatenation.
	nStr := fmt.Sprintf("%d", eq.numbers[idx])
	testStr := fmt.Sprintf("%d", eq.test)
	if len(nStr) >= len(testStr) {
		return false
	}

	strIdx := len(testStr) - len(nStr)
	if nStr != testStr[strIdx:] {
		return false
	}
	test, _ := strconv.ParseInt(testStr[:strIdx], 10, 64)
	con := &equation{test: test, numbers: eq.numbers[:idx]}
	return con.validWithConcatenation()
}

func parse(line string) (*equation, error) {
	sp := strings.Split(line, ": ")
	if len(sp) != 2 {
		return nil, errors.New("invalid equation")
	}
	test, err := strconv.ParseInt(sp[0], 10, 64)
	if err != nil {
		return nil, err
	}
	sp = strings.Split(sp[1], " ")
	numbers := make([]int64, len(sp))
	for i := range sp {
		numbers[i], err = strconv.ParseInt(sp[i], 10, 64)
		if err != nil {
			return nil, err
		}
	}
	return &equation{test: test, numbers: numbers}, nil
}

func main() {
	f, err := os.Open("input.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()

	eqs := []*equation{}

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		eq, err := parse(scanner.Text())
		if err != nil {
			log.Fatal(err)
		}
		eqs = append(eqs, eq)
	}
	if err := scanner.Err(); err != nil {
		log.Fatal(err)
	}

	total := int64(0)
	for _, eq := range eqs {
		if eq.valid() {
			total += eq.test
		}
	}
	fmt.Printf("Part 1: %d\n", total)

	total = int64(0)
	for _, eq := range eqs {
		if eq.validWithConcatenation() {
			total += eq.test
		}
	}
	fmt.Printf("Part 2: %d\n", total)
}
