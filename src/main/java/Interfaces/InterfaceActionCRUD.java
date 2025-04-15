package Interfaces;

import java.util.List;

public interface InterfaceActionCRUD <A>{
    public void addAction(A a);
    public void updateAction(A a);
    public void deleteAction(A a);
    public List<A> findAction();
}
