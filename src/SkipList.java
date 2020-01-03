
import java.util.Objects;
import java.util.Random;

public class SkipList {
    public static void main(String[] args) {
        SkipListBean skipListBean = SkipListBean.init();
        skipListBean.insert(1);
        skipListBean.insert(4);

        skipListBean.insert(3);
        skipListBean.insert(9);
        skipListBean.insert(6);
        skipListBean.insert(2);
        FindResult findResult = skipListBean.find(6);
        return;
    }



    public static class SkipListBean {
        public static final int MAXT_LEVEL = 32;
        public int length = 0;
        //除该处高度从1开始外，其他level均指levels数组中的index，从0开始计算
        public int level = 0;
        //header 不计入length,value 无效
        public Node header = new Node(0, MAXT_LEVEL);
        public Node tail;

        private Random random = new Random();

        public void insert(int value) {
            FindResult findResult = find(value);
            Node lastOrCurrent = findResult.node;
            if (lastOrCurrent.value == value) {
                System.out.println(value + "已经存在啦");
                return;
            }

            int newValueRank = findResult.rank + 1;

            insert2(value, newValueRank);

            return;
        }


        private void insert2(int value, int newValueRank) {
            int newValueLevel = randomLevel();
            Node newNode = new Node(value, newValueLevel + 1);

            //查找同时插入，从header开始
            int currentLevel = newValueLevel;
            Node currentNode = header;
            int rank = 0;
            while (currentLevel >= 0) {
                Node nextNode = currentNode.levels[currentLevel].forward;

                if (Objects.isNull(nextNode) || nextNode.value > value) {
                    insert(new FindResult(rank, currentNode), newNode, currentLevel, newValueRank);
                    currentLevel -= 1;
                    continue;
                }
                rank += currentNode.levels[currentLevel].distance;
                currentNode = nextNode;
            }

            length += 1;
            currentLevel = newValueLevel + 1;
            //长高了
            if (currentLevel > level) {
                level = currentLevel;
            }
            //跨度补足，当插入的数据并未达到最高level时，将更高level且value大于插入数据的跨度增加
            //例如，当只有1，4时，1->跨度为1，但如果插入3，1->4直接跨度需更改为2
            while (currentLevel <= level) {
                FindResult lastPoint = findInCurrentlevel(value, currentLevel);
                incrementDistance(lastPoint, currentLevel);
                currentLevel += 1;
            }
            //tail
            if (Objects.isNull(tail) || value > tail.value) {
                tail = newNode;
            }
            return;
        }

        private int randomLevel() {
            int i = 0;
            while (i < (MAXT_LEVEL - 1)) {
                if (random.nextInt() > 0.5) {
                    i += 1;
                    continue;
                }
                break;
            }
            return i;
        }

        private void insert(FindResult findResult, Node newNode, int level, int rank) {
            Node currentNode = findResult.node;
            Node next = currentNode.levels[level].forward;
            //before
            if (level == 0) {
                if (findResult.rank != 0) {
                    newNode.before = currentNode;
                }
                if (Objects.nonNull(next)) {
                    next.before = newNode;
                }
            }
            //forward
            currentNode.levels[level].forward = newNode;
            newNode.levels[level] = new LevelBean();
            newNode.levels[level].forward = next;
            //distance
            //+1 是因为本次插入了一个节点，所以跨度增加1
            newNode.levels[level].distance = Objects.nonNull(next)
                    ? currentNode.levels[level].distance - (rank - findResult.rank) + 1
                    : 0;
            currentNode.levels[level].distance = rank - findResult.rank;
        }

        private void incrementDistance(FindResult findResult, int level) {
            Node currentNode = findResult.node;
            if (currentNode.levels[level].distance != 0) {
                currentNode.levels[level].distance += 1;
            }
        }

        /**
         * 找value所在的节点，没有的话就返回最后一个小于value的节点
         *
         * @param value
         * @return
         */
        public FindResult find(int value) {
            int i = level-1;
            Node current = header;
            int rank = 0;
            while ( i >= 0 && Objects.nonNull(current)) {
                //当前level已到尾部
                Node next = current.levels[i].forward;
                if (Objects.isNull(next)) {
                    i -= 1;
                    continue;
                }
                //当前level下一个节点就过了
                if (next.value > value) {
                    i -= 1;
                    continue;
                }
                //计算一下排名
                rank += current.levels[i].distance;
                //找到了
                if (next.value == value) {
                    System.out.println("value: " + value + "; rank: " + rank);
                    return new FindResult(rank, next);
                }
                current = next;
            }
            return new FindResult(rank, current);
        }

        public FindResult findInCurrentlevel(int value, int level) {
            Node current = header;
            Node next = current.levels[level].forward;
            int rank = 0;
            while (Objects.nonNull(next)) {
                if (next.value > value) {
                    return new FindResult(rank, current);
                }
                rank += current.levels[level].distance;

                current = next;
                next = current.levels[level].forward;
            }
            return new FindResult(rank, current);
        }

        public static SkipListBean init() {
            SkipListBean skipListBean = new SkipListBean();
            LevelBean[] levelBeans = skipListBean.header.levels;
            for (int i = 0; i < MAXT_LEVEL; i++) {
                levelBeans[i] = new LevelBean();
            }
            return skipListBean;
        }

        public boolean isEmpty() {
            return length == 0;
        }

    }

    public static class Node {
        /*public String name;*/
        public int value;
        public Node before = null;
        public LevelBean[] levels;

        public Node(int value, int length) {
            this.value = value;
            levels = new LevelBean[length];
        }
    }

    public static class FindResult {
        public int rank;
        public Node node;

        public FindResult(int rank, Node node) {
            this.rank = rank;
            this.node = node;
        }
    }

    public static class LevelBean {
        public int distance = 0;
        public Node forward = null;
    }
}
