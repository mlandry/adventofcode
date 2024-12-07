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

type rule struct {
	first  int
	second int
}

func parseRule(line string) (*rule, error) {
	sp := strings.Split(line, "|")
	if len(sp) != 2 {
		return nil, errors.New("expected two parts")
	}
	first, err := strconv.Atoi(sp[0])
	if err != nil {
		return nil, err
	}
	second, err := strconv.Atoi(sp[1])
	if err != nil {
		return nil, err
	}
	return &rule{first: first, second: second}, nil
}

type ruleMap struct {
	rules map[int]map[int]bool
}

func newRuleMap() *ruleMap {
	return &ruleMap{rules: map[int]map[int]bool{}}
}

func (rm *ruleMap) addRule(r *rule) {
	v, ok := rm.rules[r.first]
	if !ok {
		v = map[int]bool{}
		rm.rules[r.first] = v
	}
	v[r.second] = true
}

type update struct {
	pages []int
}

func parseUpdate(line string) (*update, error) {
	sp := strings.Split(line, ",")
	pages := make([]int, len(sp))
	for i := range sp {
		u, err := strconv.Atoi(sp[i])
		if err != nil {
			return nil, err
		}
		pages[i] = u
	}
	return &update{pages: pages}, nil
}

func (u *update) matchesRules(rm *ruleMap) bool {
	// For each pair of numbers, make sure there is no rule that says b comes before a.
	for i, a := range u.pages {
		for _, b := range u.pages[i:] {
			if afters, ok := rm.rules[b]; ok {
				if v, ok := afters[a]; v && ok {
					return false
				}
			}
		}
	}
	return true
}

func (u *update) makeOneCorrection(rm *ruleMap) bool {
	for i, a := range u.pages {
		for j, b := range u.pages[i:] {
			if afters, ok := rm.rules[b]; ok {
				if v, ok := afters[a]; v && ok {
					u.pages[i] = b
					u.pages[j+i] = a
					return true
				}
			}
		}
	}
	return false
}

func (u *update) getMiddlePage() int {
	return u.pages[len(u.pages)/2]
}

func main() {
	f, err := os.Open("input.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()

	ruleMap := newRuleMap()
	updates := []*update{}
	rulesDone := false

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		l := scanner.Text()
		if l == "" {
			rulesDone = true
			continue
		}

		if rulesDone {
			u, err := parseUpdate(l)
			if err != nil {
				log.Fatal(err)
			}
			updates = append(updates, u)
		} else {
			r, err := parseRule(l)
			if err != nil {
				log.Fatal(err)
			}
			ruleMap.addRule(r)
		}
	}

	sum := 0
	for _, u := range updates {
		if u.matchesRules(ruleMap) {
			sum += u.getMiddlePage()
		}
	}
	fmt.Printf("Part 1: %d\n", sum)

	sum = 0
	for _, u := range updates {
		if !u.makeOneCorrection(ruleMap) {
			continue
		}
		for u.makeOneCorrection(ruleMap) {
		}
		sum += u.getMiddlePage()
	}
	fmt.Printf("Part 2: %d\n", sum)
}
