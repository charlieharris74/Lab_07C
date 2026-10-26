public class Ship
{
    private final String name;
    private final int length;
    private int hits;

    public Ship(String name, int length)
    {
        if (name == null || name.isBlank() || length < 1)
            throw new IllegalArgumentException("A ship needs a name and a positive length.");
        this.name = name;
        this.length = length;
    }

    public String getName() { return name; }
    public int getLength() { return length; }
    public int getHits() { return hits; }
    public boolean isSunk() { return hits == length; }

    public void registerHit()
    {
        if (hits < length)
            hits++;
    }
}
