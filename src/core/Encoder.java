package core;

import java.nio.ByteBuffer;
import java.util.*;
import application.*;
public final class Encoder {

  public interface Callback {
    public boolean call(Encoder encoder, EncodedPacket packet);
  }

  private static final double DEFAULT_FAILURE_PROBABILITY = 0.01;
  private static final int DEFAULT_SPIKE = 20;

  private final int filesize;
  private final int packetSize;
  private final int nPackets;
  private final Random uniformRNG;
  private final RobustSolitonGenerator solitonRNG;
  private final ByteBuffer buffer;
  private final long seed;
  private long packetCounter = 0;

  public Encoder(byte[] data, int packetSize) {
    this(data, packetSize, DEFAULT_FAILURE_PROBABILITY, DEFAULT_SPIKE);
  }

  public Encoder(byte[] data, int packetSize, double failureProbability, int spike) {
    this.filesize = data.length;
    this.packetSize = packetSize;
    this.seed = (long)(Math.random() * 1024 * 5);
    this.nPackets = (int)Math.ceil(data.length / (double)packetSize);
    this.uniformRNG = new Random(this.seed);
    this.solitonRNG = new RobustSolitonGenerator(nPackets, spike, failureProbability);

    this.buffer = ByteBuffer.wrap(Arrays.copyOf(data, nPackets * packetSize),
                                  0, nPackets * packetSize);
  }

  public long getSeed() {
    return this.seed;
  }

  public long getPacketCounter(){return this.packetCounter;}

  public int getNPackets() {
    return this.nPackets;
  }

  public int getRandom(int min, int max){
    Random random = new Random();
    return random.nextInt( max - min + 1 ) + min;
  }

  public int[] getRandoms(int min, int max, int count){
    int[] randoms = new int[count];
    List<Integer> listRandom = new ArrayList<Integer>();

    if( count > ( max - min + 1 )){
      return null;
    }
    // 将所有的可能出现的数字放进候选list
    for(int i = min; i <= max; i++){
      listRandom.add(i);
    }
    // 从候选list中取出放入数组，已经被选中的就从这个list中移除
    for(int i = 0; i < count; i++){
      int index = getRandom(0, listRandom.size()-1);
      randoms[i] = listRandom.get(index);
      listRandom.remove(index);
    }

    return randoms;
  }


  public void encode(Callback callback) {
    boolean abort = false;
    while(!abort) {
      try {

        int d = solitonRNG.next();
        while(d>20){
          d = solitonRNG.next();
        }
        if(d>Client.maxd)
        {
          Client.maxd = d;
        }
        BitSet xorSet = null;

        int[] neighbours = new int[d];
        neighbours = getRandoms(0,this.nPackets-1,d);
        for(int i = 0; i < d; i++)
        {
          //int packetIndex = uniformRNG.nextInt(this.nPackets);
          //int pos = packetIndex * this.packetSize;
          //neighbours[i] = packetIndex;
          int pos = neighbours[i] * this.packetSize;
          this.buffer.limit(pos + this.packetSize);
          this.buffer.position(pos);
          BitSet bitSet = BitSet.valueOf(this.buffer).get(0, this.packetSize * 8);
          if(xorSet == null)
            xorSet = bitSet;
          else
            xorSet.xor(bitSet);
        }

        packetCounter++;
        //System.out.println("bmb"+packetCounter);
        /*if(packetCounter>3*nPackets){
          break;
        }*/

        byte[] packetData = xorSet.toByteArray();
        DecodedPacket decodedpacket = new DecodedPacket(filesize, neighbours, packetData);        
        EncodedPacket encodedpacket = EncodedPacket.encode(decodedpacket);
        abort = callback.call(this, encodedpacket);
      } catch(Exception e) {
        /* FIXME*/
        e.printStackTrace();
      }

    }


  }
}
