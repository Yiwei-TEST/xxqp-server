package com.sy599.game.qipai.csmj.robot.btlibrary;

import jbt.execution.core.*;
import jbt.execution.core.ExecutionTask.Status;
import jbt.model.core.ModelTask;

public class HelloWorld {
    public static boolean canhu = false;
    public static void main(String[] args) throws Exception {

        /* First of all, we create the BT library. */
        IBTLibrary btLibrary = new MajiangBTLibrary();
        /* Then we create the initial context that the tree will use. */
        IContext context = ContextFactory.createContext(btLibrary);
        /*
         * Now we are assuming that the marine that is going to be *
         * controlled has an id of "terranMarine1"
         */
        context.setVariable("CurrentEntityID", "terranMarine1");

        /* Now we get the Model BT to run. */
        ModelTask terranMarineTree = btLibrary.getBT("majiang");

        /* action 赋值 */
        context.setVariable("Chi","");

        /* Then we create the BT Executor to run the tree. */
        IBTExecutor btExecutor = BTExecutorFactory.createBTExecutor(terranMarineTree, context);

        btExecutor.tick();
        context.setVariable("hu",true);
        context.setVariable("mingbu",false);
        context.setVariable("chi",false);
        context.setVariable("da",true);
        context.setVariable("minggang",false);
        context.setVariable("guo",false);
        context.setVariable("peng",false);
        context.setVariable("xiaohu",false);
        context.setVariable("zimo",false);
        context.setVariable("anbu",false);
        context.setVariable("angang",false);
        context.setVariable("userName",false);
        int step = 1;
        /* And finally we run the tree through the BT Executor. */
        do {
            System.out.println("--------------Step = " + step);
            btExecutor.tick();
            Thread.sleep(1000);
            step++;
        } while (btExecutor.getStatus() == Status.RUNNING);

        Object action =  context.getVariable("action");
        if(null == action){

        }else{
            System.out.println(action+"~~~~~~~");
        }

    }

}
