package programanalysis

import groovy.util.logging.Slf4j

@Slf4j
class ReachingDefinitions {
    List<Tuple> workList = []
    Map rdEntries = [:]
    Map rdExit = [:]
    Map<String, List<Tuple>> rdKill = [:]
    Map<String, Tuple> rdGen = [:]

    Map<String, List<Tuple>> rdAnalysisWithFIFO(List<Block> program) {
        program.each { Block block ->
            rdEntries[block.label] = []
            addEdgesToEndOfWorkList(block)
            if (block.variableAssigned) {
                List<Tuple> influencedBlocks = rdKill.values().find {
                    it.findAll { it.first() == block.variableAssigned }
                }
                influencedBlocks.each { Tuple blockTuple ->
                    String variable = blockTuple.first()
                    String influencedBlockLabel = blockTuple.last()
                    rdKill[influencedBlockLabel] = rdKill[influencedBlockLabel] + [new Tuple(variable, block.label)]
                }
                rdKill[block.label] = influencedBlocks ?
                        influencedBlocks + [new Tuple(block.variableAssigned, block.label)] :
                        [new Tuple(block.variableAssigned, block.label)]

                rdGen[block.label] = [new Tuple(block.variableAssigned, block.label)]
            }
        }

        String workListToString = workList.join('\n')
        log.info workListToString

        while (!workList.empty) {
            Tuple workListItem = workList.first()
            workList = workList.drop(1)
            calculateSolution(workListItem.first(), workListItem.last())

            Block lPrimeBlock = program.find { it.label == workListItem.last() }
            addEdgesToEndOfWorkList(lPrimeBlock)
        }

        //to calculate exit of last block
        calculateSolution(new Tuple(program.last().label, null) )

        return rdExit
    }

    private addEdgesToEndOfWorkList(Block block) {
        block.outputs.each {
            workList << new Tuple(block.label, it)
        }
    }

    void calculateSolution(String l, String lPrime) {
        rdExit[l] = (rdEntries[l] - rdKill[l])
        if (rdGen[l]) {
            rdExit[l] = rdExit[l] + rdGen[l]
        }
        if (lPrime) {
            if (rdExit[l].any { !(it in rdEntries[lPrime]) }) {
                List<Tuple> tuplesNotInResult = rdExit[l].findAll { !(it in rdEntries[lPrime]) }
                rdEntries[lPrime].addAll(tuplesNotInResult)
            }
        }
    }
}
