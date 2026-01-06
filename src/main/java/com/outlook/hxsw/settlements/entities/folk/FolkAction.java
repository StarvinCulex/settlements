package com.outlook.hxsw.settlements.entities.folk;

/**
 * 这个对象将在逻辑线程构造，但是会移动到游戏主线程执行。
 */
public abstract class FolkAction<A> {
    /** 启动动作。从零开始执行，或因有更高优先级的行为暂时打断任务后继续执行，都会调用。 */
    protected abstract void start();

    /** 每一刻都会调用。当这个动作完成，已经不需要再做，就在这里调用`complete`方法。 */
    protected abstract void tick();

    /** 因更高优先级的行为暂时打断时调用。不代表取消了任务。 */
    protected abstract void interrupt();

    /** 任务被取消后调用。被取消的前提一定是任务被打断了。 */
    protected abstract void cancel();

    protected final void complete(A output) {
        if (!completed) {
            this.entity.onActionCompleted(this, output);
            completed = true;
        }
    }

    protected final FolkEntity entity() {
        return entity;
    }

    private FolkEntity entity;
    private boolean completed = false;
    final void setFolkEntity(FolkEntity entity) {
        if (this.entity != null) {
            throw new RuntimeException();
        }
        this.entity = entity;
    }
}
