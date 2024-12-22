package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"
)

type onsen struct {
	towels map[string]bool
	cache  map[string]int64
}

func (o *onsen) possibilities(design string) int64 {
	if count, ok := o.cache[design]; ok {
		return count
	}
	count := int64(0)
	if o.towels[design] {
		count++
	} else if len(design) == 1 {
		return 0
	}
	for i := 1; i < len(design); i++ {
		if o.towels[design[:i]] {
			count += o.possibilities(design[i:])
		}
	}
	o.cache[design] = count
	return count
}

func main() {
	file, err := os.Open("input.txt")
	if err != nil {
		panic(err)
	}

	onsen := &onsen{}
	var designs []string

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()
		if line == "" {
			continue
		}
		if onsen.towels == nil {
			onsen.towels = map[string]bool{}
			sp := strings.Split(line, ", ")
			for _, s := range sp {
				onsen.towels[s] = true
			}
			continue
		}
		designs = append(designs, line)
	}

	onsen.cache = map[string]int64{}
	possible := 0
	possibilities := int64(0)
	for _, design := range designs {
		p := onsen.possibilities(design)
		if p > 0 {
			possible += 1
		}
		possibilities += p
	}
	fmt.Printf("Part 1: %d\n", possible)
	fmt.Printf("Part 2: %d\n", possibilities)
}
