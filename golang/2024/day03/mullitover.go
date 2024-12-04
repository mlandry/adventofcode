package main

import (
	"fmt"
	"log"
	"os"
	"strconv"
)

type status int

const (
	none status = iota
	mul
	dont
)

func isDigit(v rune) bool {
	return v >= '0' && v <= '9'
}

func processInput(input string, alwaysEnabled bool) int64 {
	prev := rune(0)
	first := int64(0)
	second := int64(0)
	comma := false
	st := none
	enabled := true

	reset := func() {
		st = none
		first = 0
		second = 0
		comma = false
	}

	sum := int64(0)

	for _, v := range input {
		switch st {
		case none:
			if v == 'm' {
				st = mul
			} else if v == 'd' {
				st = dont
			}
		case mul:
			switch {
			case prev == 'm':
				if v != 'u' {
					reset()
				}
			case prev == 'u':
				if v != 'l' {
					reset()
				}
			case prev == 'l':
				if v != '(' {
					reset()
				}
			case prev == '(':
				if isDigit(v) {
					first = int64(v - '0')
				} else {
					reset()
				}
			case prev == ',':
				if isDigit(v) {
					second = int64(v - '0')
				} else {
					reset()
				}
			case isDigit(prev):
				if v == ',' && !comma {
					comma = true
				} else if v == ')' && comma {
					if enabled || alwaysEnabled {
						sum += (first * second)
					}
					reset()
				} else if isDigit(v) {
					if comma {
						second *= 10
						second += int64(v - '0')
					} else {
						first *= 10
						first += int64(v - '0')
					}
				} else {
					reset()
				}
			default:
				reset()
			}
		case dont:
			switch {
			case prev == 'd':
				if v != 'o' {
					reset()
				}
			case prev == 'o':
				if v != 'n' {
					enabled = true
					reset()
				}
			case prev == 'n':
				if v != '\'' {
					reset()
				}
			case prev == '\'':
				if v == 't' {
					enabled = false
				}
				reset()
			}
		}
		prev = v
	}
	return sum
}

func main() {
	f, err := os.ReadFile("input.txt")
	if err != nil {
		log.Fatal(err)
	}
	input := string(f)

	sum := processInput(input, true)
	fmt.Println("Part 1: " + strconv.FormatInt(sum, 10))

	sum = processInput(input, false)
	fmt.Println("Part 2: " + strconv.FormatInt(sum, 10))
}
