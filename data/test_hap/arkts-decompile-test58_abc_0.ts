export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        try {
        } catch (e) {
            throw v2;
        }
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            v6.info(v7, v8, v9);
            return;
        }
        const v8 = "testTag";
        const v9 = "Failed to load the content. Cause: %{public}s";
        const v11 = JSON;
        v11.stringify(v16);
        stringify = v11.stringify(v12);
        v6.error(v7, v8, v9);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_0 = 0;
        v4 = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        try {
        } catch (e) {
            const error = v8.error;
            const v10 = "testTag";
            const v11 = "Failed to set colorMode. Cause: %{public}s";
            const v13 = JSON;
            let stringify = v13.stringify;
            const v14 = v6;
            v13.stringify(v14);
            stringify = v13.stringify(v14);
            v8.error(v9, v10, v11);
        }
        const v9 = "testTag";
        const v10 = "%{public}s";
        const v11 = "Ability onCreate";
        error.info(v8, v9, v10);
        return;
    }

    static onDestroy(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v9 = "%{public}s";
        v6.info(v7, v8, v9);
        const v6 = v3;
        const v7 = "pages/Index";
        v8 = onBackground;
        v6.loadContent(v7, v8);
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        try {
        } catch (e) {
            throw v2;
        }
    }

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_0 = 0;
        v4 = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        try {
            throw v5;
        } catch (e) {
            throw v3;
        }
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        try {
            throw v6;
        } catch (e) {
            throw v4;
        }
    }
}
export class Index& {
    pkgName@entry;

    constructor(param_0, param_1, param_2)     {
        return v6;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static on(param_0, param_1, param_2, param_3, param_4): void     {
        let v9 = v2;
        const listeners = v9.listeners;
        let get = listeners.get;
        v9 = v15;
        listeners.get(v9);
        get = listeners.get(v9);
        get = get2;
        if (!(get === undefined)) {
            const isUndefined: boolean = get === undefined;
            let push = isUndefined.push;
            let v9 = v4;
            isUndefined.push(v9);
            return;
        }
        const isUndefined: Array<unknown> = [];
        let push: Array<unknown> = [];
        let v9: Array<unknown> = push;
        push = v9.push;
        v9.push(v16);
        v10 = v14;
        const listeners = v10.listeners;
        v10 = v15;
        listeners.set(v10, push);
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Column;
        v7.create();
        v7 = Column;
        v7.width("100%");
        v7 = Column;
        v8 = "100%";
        v7.height(v8);
        return;
    }

    static emit(param_0, param_1, param_2, param_3): void     {
        let v9 = v2;
        const listeners = v9.listeners;
        let get = listeners.get;
        listeners.get(v16);
        get = listeners.get(v16);
        get = get2;
        if (!(get === undefined)) {
            let isUndefined: boolean = get === undefined;
            isUndefined = get === undefined;
            const listeners = 0;
            let v9 = listeners;
            const v10 = get2;
            while (!(v9 < v10.length)) {
                loop_0: do {
    let hasLength: boolean = v9 < v10.length;
    let v10 = get2;
} while (hasLength < v10.length);
            }
            return hasLength < v10.length;
        }
        let isUndefined: Array<unknown> = [];
        return [];
    }

    static sort(param_0, param_1, param_2, param_3): void     {
        let v7 = v3;
        v7.slice(0);
        slice = v7.slice(v8);
        v7 = 0;
        const v10 = slice2;
        let length = v10.length;
        let length = 0;
        const v10: number = length;
        length = slice2.length;
        let v11: number = length2 - 1;
        let v11 = slice2;
        let length2 = length;
        const v10 = v11[length2];
        v11 = slice2;
        length2 = length + 1;
        if (!(v10 > v11[length2])) {
            const v10: boolean = v10 > v11[length2];
            const number: number = Number(v10);
            let length: number = _number + 1;
        }
        let v11 = slice2;
        let length2 = length;
        const _number = v11[length2];
        v11 = slice2;
        length2 = length;
        let v13 = slice2;
        v11[length2] = v13[v14];
        v11 = slice2;
        v13 = length;
        length2 = v13 + 1;
        v11[length2] = _number;
    }

    static sort(param_0, param_1, param_2, param_3): void     {
        let v7 = v3;
        v7.slice(0);
        slice = v7.slice(v8);
        v7 = 0;
        v8 = v7;
        let v10 = slice2;
        const length = v10.length;
        let v8 = v7;
        const v11 = v7;
        let v10: number = v11 + 1;
        const v11 = v10;
        let v12 = slice2;
        while (!(v11 < v12.length)) {
            let v12 = slice2;
            let v13 = v10;
            const v11 = v12[v13];
            v12 = slice2;
            v13 = v8;
            if (v11 < v12[v13]) {
                let v8 = v10;
                const v11 = v10;
                const number: number = Number(v11);
                let v10: number = _number + 1;
                continue;
            }
        }
        const length: boolean = v11 < v12[v13];
        if (!(length !== v7)) {
            let v8: boolean = length !== v7;
            const number: number = Number(v8);
            let v7: number = _number2 + 1;
        }
        let v10 = slice2;
        let _number = v7;
        const length = v10[_number];
        v10 = slice2;
        _number = v7;
        let v12 = slice2;
        let v13 = _number2;
        v10[_number] = v12[v13];
        v10 = slice2;
        _number = _number2;
        v10[_number] = length;
    }

    static sort(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const strategy = v7.strategy;
        strategy.sort(v11);
        return strategy.sort(v11);
    }

    static next(param_0, param_1, param_2): void     {
        const v6 = v2;
        const data = v6.data;
        const v7 = v2;
        let index = v7.index;
        const v3 = data[index];
        index = v10;
        index = index.index;
        const number: number = Number(index2);
        index.index = _number + 1;
        return v3;
    }

    static reset(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.index = 0;
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Text;
        v7.create(lex_0_0.message);
        v7 = Text;
        message = 30;
        v7.fontSize(message);
        v7 = Text;
        message = FontWeight;
        v7.fontWeight(message.Bold);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        let v11 = ViewPU;
        let v11 = method_63;
        let set = v11.set;
        v11.set(ViewPU.prototype, "finalizeConstruction", on);
        prototype = undefined;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        v14 = "message";
        let v15 = undefined;
        false[v16] = "get".v15;
        v14 = "message";
        v15 = undefined;
        v16 = RemoteControl;
        false[v15] = "get".v16;
        prototype2.initialRender = initialRender;
        prototype2.rerender = getListenerCount;
        set.getEntryName = SelectionSortStrategy;
        set = set;
        set = set;
        set = registerNamedRoute;
        v11 = func_56;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static turnOn(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.state = true;
        return;
    }

    static turnOff(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.state = false;
        return;
    }

    static pressOn(param_0, param_1, param_2): void     {
        let v6 = v2;
        const light = v6.light;
        light.turnOn();
        v6 = v9;
        const history = v6.history;
        v6 = "on";
        history.push(v6);
        return;
    }

    static hasNext(param_0, param_1, param_2): void     {
        const v5 = v2;
        const index = v5.index;
        const v6 = v2;
        const data = v6.data;
        return index < data.length;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static testCommand(param_0, param_1, param_2): void     {
        let acc: acc = new acc();
        acc = new acc(v7, acc);
        v8 = acc2;
        let pressOn = v8.pressOn;
        v8.pressOn();
        v8 = acc2;
        v8.pressOff();
        v8 = acc2;
        pressOn = v8.pressOn;
        v8.pressOn();
        pressOn = acc;
        const state = pressOn.state;
        let v9 = String;
        v9 = v9(state);
        pressOn = `${v9},`;
        v9 = acc2;
        v9.getHistory();
        return pressOn + v9.getHistory();
    }

    static pressOff(param_0, param_1, param_2): void     {
        let v6 = v2;
        const light = v6.light;
        light.turnOff();
        v6 = v9;
        const history = v6.history;
        v6 = "off";
        history.push(v6);
        return;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static testIterator(param_0, param_1, param_2): void     {
        const v8: Array<unknown> = [];
        const v4 = new [](v7);
        const v7: Array<unknown> = [];
        while (true) {
            const v8 = v4;
            let next = v8.hasNext;
            v8.hasNext();
            next = undefined;
            const v10 = v4;
            next = v10.next;
            v10.next();
            next = v10.next();
            v8.push(next2);
        }
    }

    static testStrategy(param_0, param_1, param_2): void     {
        const v8: Array<unknown> = [];
        const v3: Array<unknown> = [];
        let acc: acc = new acc(v10);
        acc = new new acc(v10)(v8, acc, v10);
        let sort = acc.sort;
        const v10: Array<unknown> = v3;
        acc.sort(v10);
        sort = acc.sort(v10);
        acc = v6;
        acc = new acc(v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20);
        acc.setStrategy(acc2);
        acc = v6;
        sort = acc.sort;
        const acc2: Array<unknown> = v3;
        acc.sort(acc2);
        sort = acc.sort(acc2);
        let v11 = sort2;
        let join = v11.join;
        const v12 = ",";
        v11.join(v12);
        join = v11.join(v12);
        sort = `${join2}|`;
        join = sort3;
        join = join.join;
        v11 = ",";
        join.join(v11);
        return sort + join.join(v11);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static getHistory(param_0, param_1, param_2): void     {
        let v6 = v2;
        const history = v6.history;
        v6 = ",";
        history.join(v6);
        return history.join(v6);
    }

    static setStrategy(param_0, param_1, param_2, param_3): void     {
        v2 = v8;
        v2.strategy = v9;
        return;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testEventEmitter(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v9: acc = acc;
        v9.on("click", 1);
        v9 = acc;
        on = v9.on;
        v10 = "click";
        v11 = 2;
        v9.on(v10, v11);
        v9 = acc;
        on = v9.on;
        v10 = "hover";
        v11 = 3;
        v9.on(v10, v11);
        v9 = acc;
        v10 = "click";
        v9.getListenerCount(v10);
        listenerCount = v9.getListenerCount(v10);
        v9 = acc;
        listenerCount = v9.getListenerCount;
        v10 = "hover";
        v9.getListenerCount(v10);
        listenerCount = v9.getListenerCount(v10);
        v9 = acc;
        let emit = v9.emit;
        v10 = "click";
        v9.emit(v10);
        emit = v9.emit(v10);
        let v12 = String;
        v12 = v12(listenerCount2);
        v10 = `${v12},`;
        v12 = String;
        v12 = listenerCount3;
        v9 = v10 + v12(v12);
        emit = `${v9},`;
        v10 = emit2;
        v12 = ",";
        v10.join(v12);
        return emit + v10.join(v12);
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5.observeComponentCreation2(func_main_0, Column);
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = onWindowStageCreate;
        v7 = Text;
        v5.observeComponentCreation2(v6, v7);
        v5 = Text;
        v5.pop();
        v5 = Column;
        pop = v5.pop;
        v5.pop();
        return;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static getListenerCount(param_0, param_1, param_2, param_3): void     {
        let v8 = v2;
        const listeners = v8.listeners;
        let get = listeners.get;
        listeners.get(v12);
        get = listeners.get(v12);
        get = get2;
        if (!(get === undefined)) {
            const isUndefined: boolean = get === undefined;
            return isUndefined.length;
        }
        return 0;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.aboutToBeDeleted();
        let v7 = SubscriberManager;
        v7.Get();
        get = v7.Get();
        let id__ = v7.id__;
        v10.id__();
        id__ = v10.id__();
        get2.delete(id__);
        const get2 = v2;
        get2.aboutToBeDeletedInternal();
        return;
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const message = v6.message;
        const message = v2;
        const v6 = v3;
        message.message = v6.message;
        return;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.purgeDependencyOnElmtId(v11);
        return;
    }
}
export class @ohos.app {
    @native.ohos.app;
}
export class @ohos.curves {
    @native.ohos.curves;
}
export class @ohos.matrix4 {
    @native.ohos.matrix4;
}
export class @system.app {
    @native.system.app;
}
export class @system.curves {
    @native.system.curves;
}
export class @system.matrix4 {
    @native.system.matrix4;
}
export class @system.router {
    @native.system.router;
}
export class _ESConcurrentModuleRequestsAnnotation {
}
export class _ESExpectedPropertyCountAnnotation {
}
export class _ESSlotNumberAnnotation {
}