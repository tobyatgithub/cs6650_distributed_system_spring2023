# CS6650 Distributed System Spring2023

Repo for the distributed system 2023 Spring at NEU.

## Class Notes

### Feb.04 2023, self-learning note.
> The lecture this Friday == guest speaker Peter Smith. See the obsidian note for the take aways.

Here's the notes on doing & learning Homework 4 (~ #LittlesLaw, #multithread)
- Little's Law indicate that **the average number of items in a system (inventory) is equal to the average arrival rate multiplied by the average time that each item spends in the system.** i.e. L = λ * W
- λ = throughput (request/second) = average arrival rate of requests = total number of requests / wall time.
- L = average number of requests in process or system.
- W = latency (seconds) = average response time per request.



### Jan. 27 2023, third lecture

- Peter's coming next week! Microservice

- 20 mins working on hw2 and hw3.

  - metric, log, multi-thread!

- Mapreduce papre
  - They changed file system! They designed a specific file system for this map reduce work.
  - More task specific file system shall be developed!

### Jan.20 2023, second lecture

---

Intro and recap:

- First start with homework:
  - google tutotiral
  - podc papers
  - aws
  - DSFP = distributed system for fun and profit (in the syllabus resource)

### Jan.13 2023, first lecture.

---

- Motivation, class structure, and expectation (AWS).
- Failure modes (surviving). Redundancy (then persistency problem).
- Works in ms level, and how do we keep data synced? Even worse, our clock might not be exactly the same! (google: atomic clock)
- All matters: network, clock, node, communication, client-server architecture.
- Lock free data structures -> driving the performance improvement in the distributed system.
- 2 phase commit. 3 phase commit.
- consensus algorithm (Raff).
- Data intensive computing.
- Scalability issue.

- Queuing, caching, monitoring.
- Distributed database issues. More design patterns for data intensive computing.

- microservice vs api: (**the key point is dynamic**) more elastic -> work concurrently. & (define module vs. object) -> more about scalability factor, swap out/in.

- Nonfunctional requirements

  - Performance, performance, performance.
  - collecting data on performance generates a lot of data
  - analysis of this is also challenging

  - Scale: more, more, more. With dynamic spikes

  - Security: data and access control

  - Available, hopefully 24/7

- Scalability!

  - being ale to increase and decrease on demand.
  - one of the key motivation of distributed system is to accommodate the increased load (and scale it down to save cost when the traffic is slow)
  - pre warm up.
  - [Amdahl's Law](https://en.wikipedia.org/wiki/Amdahl%27s_law): how parallel it is ~ how much benefit it can receive from distributed system.

- Tradeoffs
  - throughput vs. cost
  - performance vs. availability
  - need to be able to measure to discuss
