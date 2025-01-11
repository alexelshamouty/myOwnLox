package org.craftinginterpreters.playground;

class  Worker implements StuffAtHome.doWork{
    // A worker is expected to do a bunch of stuff, open the door, chase the dog, cook a meal
    @Override public String cookAMeal(Meal meal){
        return "Meal"+ meal.meal +"cooked";
    }

    @Override public Boolean chaseDog(Dog dog){
        return dog.findDog; //dog is not found
    }

    @Override public Integer openDoor(Door door){
        return door.numberOfDoors; //number of doors opened
    }
}


// These are all stuff at home I do not want to do myself. I want to delegate it to someone else. I should just define the behaviour they are allowed to do and they they define how they do it themeselves.
abstract class StuffAtHome{
    interface doWork<R> {
        R openDoor(Door door);
        R chaseDog(Dog dog);
        R cookAMeal(Meal meal);
    }
    abstract <R> R accept(doWork<R> worker);
}

class Door extends StuffAtHome{
    int numberOfDoors = 100;
    @Override <R> R accept(doWork<R> worker){
        return worker.openDoor(this);
    }
}

class Dog extends StuffAtHome{
    Boolean findDog = false;
    @Override <R> R accept(doWork<R> worker){
        return worker.chaseDog(this);
    }
}

class Meal extends StuffAtHome{
    String meal = "Stake";
    @Override <R> R accept(doWork<R> worker){
        return worker.cookAMeal(this);
    }
}

class AbstractVisitor {
    public static void main(String[] args) {
        //Let's create our stuff first
        Dog dog = new Dog();
        Meal meal = new Meal();
        Door door = new Door();

        //Let's create our worker
        Worker worker = new Worker();

        //Now let's do stuff
        System.out.println(dog.accept(worker));
        System.out.println(meal.accept(worker));
        System.out.println(door.accept(worker));
    }
}