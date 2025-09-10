package ca.awoo.microwave.hell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;

import org.junit.jupiter.api.Test;

import ca.awoo.microwave.Ref;

public class ECSTest {
    @Test
    public void oneComponent(){
        ECS ecs = new ECS();
        for(int i = 0; i < 10000; i++){
            long entity = ecs.createEntity();
            ecs.addComponent(entity, Integer.toString(i));
        }
        Ref<Integer> calls = new Ref<>(0);
        for(int i = 0; i < 1000; i++){
            ecs.query((entity, components) -> {
                calls.contents = calls.contents+1;
            }, String.class);
        }
        assertEquals(10000*1000, calls.contents);
    }

    @Test
    public void commonAndSparse(){
        ECS ecs = new ECS();
        for(int i = 0; i < 10000; i++){
            long entity = ecs.createEntity();
            ecs.addComponent(entity, Integer.toString(i));
            if(i%100 == 0){
                ecs.addComponent(entity, new Date(entity));
            }
        }
        Ref<Integer> calls = new Ref<>(0);
        for(int i = 0; i < 1000; i++){
            ecs.query((entity, components) -> {
                calls.contents = calls.contents+1;
            }, String.class, Date.class);
        }
        assertEquals(100*1000, calls.contents);
        System.out.println(calls.contents);
    }
}
