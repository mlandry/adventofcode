package main

import (
	"errors"
	"fmt"
	"log"
	"math"
	"os"
	"strconv"
	"strings"
)

type chunk struct {
	index  int
	length int
	next   *chunk
	prev   *chunk
}

type filesystem struct {
	blocks []int64
}

func newFileSystem(input string) *filesystem {
	fs := &filesystem{blocks: []int64{}}
	file := true
	id := int64(0)
	for _, r := range input {
		i := int(r - '0')
		if file {
			fs.addFile(id, i)
			id += 1
		} else {
			fs.addFreeSpace(i)
		}
		file = !file
	}
	return fs
}

func (fs *filesystem) addFile(id int64, length int) {
	for i := 0; i < length; i++ {
		fs.blocks = append(fs.blocks, id)
	}
}

func (fs *filesystem) addFreeSpace(length int) {
	for i := 0; i < length; i++ {
		fs.blocks = append(fs.blocks, -1)
	}
}

func (fs *filesystem) findNextFreeSpace(start int) (int, error) {
	for i := start; i < len(fs.blocks); i++ {
		if fs.blocks[i] == -1 {
			return i, nil
		}
	}
	return 0, errors.New("EOF")
}

func (fs *filesystem) findFreeSpaceChunks() *chunk {
	var head, curr, prev *chunk
	for i, b := range fs.blocks {
		if b != -1 {
			if curr != nil {
				curr.length = i - curr.index
				prev = curr
				curr = nil
			}
			continue
		}
		if curr == nil {
			curr = &chunk{index: i}
			if prev != nil {
				prev.next = curr
				curr.prev = prev
			}
			if head == nil {
				head = curr
			}
		}
	}
	return head
}

func (fs *filesystem) compact() {
	free, err := fs.findNextFreeSpace(0)
	if err != nil {
		return
	}

	for idx := len(fs.blocks) - 1; idx > free; idx-- {
		if fs.blocks[idx] == -1 {
			continue
		}
		fs.blocks[free] = fs.blocks[idx]
		fs.blocks[idx] = -1
		free, err = fs.findNextFreeSpace(free + 1)
		if err != nil {
			return
		}
	}
}

func (fs *filesystem) defrag() {
	// First calculate all head space chunks.
	head := fs.findFreeSpaceChunks()

	// Now work backwards from the end moving files into the first chunk they fit.
	var fileId *int64
	length := 0
	lastMoved := int64(math.MaxInt64)
	for i := len(fs.blocks) - 1; i > 0; i-- {
		b := fs.blocks[i]
		if fileId != nil && b != *fileId {
			if *fileId < lastMoved {
				// Find the first free space chunk that can accomodate the file size.
				var fit *chunk
				for chunk := head; chunk != nil && chunk.index < i; chunk = chunk.next {
					if chunk.length >= length {
						fit = chunk
						break
					}
				}
				if fit != nil {
					lastMoved = *fileId
					for j := 0; j < length; j++ {
						fs.blocks[j+fit.index] = *fileId
						fs.blocks[i+1+j] = -1
					}
					head = fs.findFreeSpaceChunks()
				}
			}

			fileId = nil
			length = 0
		}
		if b != -1 {
			if fileId == nil {
				fileId = &b
			}
			length++
		}
	}
}

func (fs *filesystem) checksum() int64 {
	chksm := int64(0)
	for i, b := range fs.blocks {
		if b > 0 {
			chksm += (int64(i) * b)
		}
	}
	return chksm
}

func (fs *filesystem) String() string {
	sb := strings.Builder{}
	for _, b := range fs.blocks {
		if b == -1 {
			sb.WriteRune('.')
		} else {
			sb.WriteString(strconv.FormatInt(b, 10))
		}
	}
	return sb.String()
}

func main() {
	f, err := os.ReadFile("input.txt")
	if err != nil {
		log.Fatal(err)
	}

	input := string(f)

	fs := newFileSystem(input)
	fs.compact()
	fmt.Printf("Part 1: %d\n", fs.checksum())

	fs = newFileSystem(input)
	fs.defrag()
	fmt.Printf("Part 2: %d\n", fs.checksum())
}
