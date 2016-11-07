package programanalysis.blocktypes

import programanalysis.Block

class Break extends Block {
    String breakTo

    @Override
    String toString() {
        super.toString() + "\nbreakTo: ${this.breakTo}\n"
    }
}
