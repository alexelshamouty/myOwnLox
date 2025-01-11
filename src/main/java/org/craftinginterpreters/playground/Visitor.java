package org.craftinginterpreters.playground;

interface PastryVisitor{
    void eatChocolateBread();
    void eatCheeseBread();
}
abstract class Pastry{
    abstract void accept(PastryVisitor visitor);
}

class chocolateBread extends Pastry{
    @Override
    void accept(PastryVisitor visitor){
        visitor.eatChocolateBread();
    }
}

class cheeseBread extends Pastry{
    @Override
    void accept(PastryVisitor visitor){
        visitor.eatCheeseBread();
    }
}

class Visitor implements PastryVisitor{
    @Override
    public void eatCheeseBread(){
        System.out.println("Yum Yum Cheesey Bread");
    }

    @Override
    public void eatChocolateBread(){
        System.out.println("Chocolate Bread!");
    }

    public static void main(String[] args){
        Visitor visitor = new Visitor();
        cheeseBread cheesey = new cheeseBread();
        chocolateBread choco = new chocolateBread();
        System.out.println("Play ground");

        cheesey.accept(visitor);
        choco.accept(visitor);
    }

}