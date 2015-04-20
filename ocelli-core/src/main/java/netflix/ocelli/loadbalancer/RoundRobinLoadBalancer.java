package netflix.ocelli.loadbalancer;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Very simple LoadBlancer that when subscribed to gets an ImmutableList of active clients 
 * and round robins on the elements in that list
 * 
 * @author elandau
 *
 * @param <Client>
 */
public class RoundRobinLoadBalancer<C> extends SettableLoadBalancer<C> {
    public static <C> RoundRobinLoadBalancer<C> create() {
        return create(-1);
    }
    
    public static <C> RoundRobinLoadBalancer<C> create(int seedPosition) {
        return new RoundRobinLoadBalancer<C>(seedPosition);
    }

    private final AtomicInteger position;

    public RoundRobinLoadBalancer() {
        this.position = new AtomicInteger(new Random().nextInt(1000));
    }
    
    public RoundRobinLoadBalancer(int seedPosition) {
        this.position = new AtomicInteger(seedPosition);
    }

    @Override
    public C next() throws NoSuchElementException {
        List<C> local = clients.get();
        if (local.isEmpty()) {
            throw new NoSuchElementException("No servers available in the load balancer");
        }
        else {
            int pos = position.incrementAndGet();
            while (pos < 0) {
                if (position.compareAndSet(pos, 0)) {
                    pos = 0;
                    break;
                }
                pos = position.incrementAndGet();
            }
            return local.get(pos % local.size());
        }
    }
}
