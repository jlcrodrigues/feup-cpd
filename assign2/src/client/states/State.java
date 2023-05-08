package client.states;


public interface State {
    State step();

    default void printTitle(String title) {
        System.out.print("\t");
        for (int i = 0; i < title.length() + 4; i++) {
            System.out.print("*");
        }
        System.out.print("\n");

        System.out.println("\t* " + title + " *");

        System.out.print("\t");
        for (int i = 0; i < title.length() + 4; i++) {
            System.out.print("*");
        }
        System.out.print("\n");
    }

    default void breakLn() {
        for (int i = 0; i < 50; i++) System.out.print("\n");
    }
}
