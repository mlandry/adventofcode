package main

import (
	"bufio"
	"log"
	"os"
	"slices"
	"strconv"
	"strings"
)

func abs(v int64) int64 {
	if v >= int64(0) {
		return v
	}
	return -v
}

func freqMap(ids []int64) map[int64]int64 {
	f := map[int64]int64{}
	for _, id := range ids {
		v, ok := f[id]
		if ok {
			f[id] = v + 1
		} else {
			f[id] = 1
		}
	}
	return f
}

func main() {
	f, err := os.Open("input.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()

	scanner := bufio.NewScanner(f)
	var ids1, ids2 []int64
	for scanner.Scan() {
		sp := strings.Fields(scanner.Text())
		id1, err := strconv.ParseInt(sp[0], 10, 64)
		if err != nil {
			log.Fatal(err)
		}
		ids1 = append(ids1, id1)

		id2, err := strconv.ParseInt(sp[1], 10, 64)
		if err != nil {
			log.Fatal(err)
		}
		ids2 = append(ids2, id2)
	}

	// Part 1.
	slices.Sort(ids1)
	slices.Sort(ids2)
	sum := int64(0)
	for i := range ids1 {
		sum += abs(ids1[i] - ids2[i])
	}
	log.Println("Part 1: " + strconv.FormatInt(sum, 10))

	// Part 2.
	fm1 := freqMap(ids1)
	fm2 := freqMap(ids2)

	sum = int64(0)
	for id, f1 := range fm1 {
		f2, ok := fm2[id]
		if !ok {
			continue
		}
		sum += (id * f1 * f2)
	}
	log.Println("Part 2: " + strconv.FormatInt(sum, 10))
}
