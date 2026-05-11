export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw v6;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            return;
        }
        const v11 = JSON;
        stringify = v11.stringify(v16);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        const v13 = JSON;
        stringify = v13.stringify(v6);
    }

    static onDestroy(param_0, param_1, param_2): void     {
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v6 = v3;
        v8 = onBackground;
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw v6;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        throw v3;
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        throw v4;
    }
}
export class Index& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        v2 = v8;
        v2._val = v9;
        v5 = v2;
        v5._listeners = 0;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static pop(param_0, param_1, param_2): void     {
        const v6 = v2;
        let items = v6.items;
        const length = items.length;
        if (!(length === 0)) {
            const v6: boolean = length === 0;
            items = v6.items;
            const pop = items.pop;
            return items.pop();
        }
        return undefined;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static addX(param_0, param_1, param_2, param_3): void     {
        const v5 = lex_0_0;
        return v5 + v9;
    }

    static peek(param_0, param_1, param_2): void     {
        const v6 = v2;
        let items = v6.items;
        let length = items.length;
        if (!(length === 0)) {
            let items: boolean = length === 0;
            items = items.items;
            const v8 = v2;
            items = v8.items;
            length = items3.length;
            items = length - 1;
            return items2[items];
        }
        return undefined;
    }

    static push(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const items = v7.items;
        return;
    }

    static size(param_0, param_1, param_2): void     {
        const v5 = v2;
        const items = v5.items;
        return items.length;
    }

    static multiply(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        return v6 * v11;
    }

    static value(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4._val;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v8 = lex_0_0;
        v7 = Text;
        message = 30;
        v7 = Text;
        message = FontWeight;
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_2 = func_main_0;
        v12 = anonymous_method;
        lex_0_1 = v12;
        v12 = func_main_0;
        lex_0_3 = v12;
    }

    static makeAdder(param_0, param_1, param_2, param_3): void     {
        lex_0_0 = v9;
        return func_main_0;
    }

    static double(param_0, param_1, param_2, param_3): void     {
        const v5 = lex_0_3;
        const v6 = 2;
        const v7 = v3;
        return v5(v6, v7);
    }

    static triple(param_0, param_1, param_2, param_3): void     {
        const v5 = lex_0_3;
        const v6 = 3;
        const v7 = v3;
        return v5(v6, v7);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     { }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static cloneRecord(param_0, param_1, param_2, param_3): void     {
        let v8 = Object;
        keys = v8.keys(v17);
        v8 = 0;
        const v9 = v8;
        const v10 = keys2;
        while (!(v9 < v10.length)) {
        }
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static testCurrying(param_0, param_1, param_2): void     {
        let v8 = lex_0_1;
        v8 = v8(5);
        v8 = lex_0_1;
        v9 = 10;
        v8 = v8(v9);
        v8 = v8;
        v9 = 3;
        v8 = v8(v9);
        v8 = v82;
        v9 = 7;
        v8 = v8(v9);
        let v10 = String;
        v10 = v10(v83);
        v8 = `${v10},`;
        v10 = String;
        return v8 + v84(v84);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static testDeepClone(param_0, param_1, param_2): void     {
        {  }[v7] = 10;
        v6 = v4;
        v7 = "y";
        v6[v7] = 20;
        v6 = lex_0_2;
        v7 = v4;
        v6 = v6(v7);
        v6 = v6;
        v7 = "x";
        v6[v7] = 99;
        let v8 = String;
        let v9 = v4;
        v9 = v9[v10];
        v8 = v8(v9);
        v6 = `${v8},`;
        v8 = String;
        v8 = v6;
        v9 = "x";
        v8 = v8[v9];
        return v6 + v8(v8);
    }

    static testObjectRest(param_0, param_1, param_2): void     {
        {  }[v8] = 1;
        v7 = v5;
        v8 = "b";
        v7[v8] = 2;
        v7 = v5;
        v8 = "c";
        v7[v8] = 3;
        v8 = Object;
        keys = v8.keys(v5);
        v8 = 0;
        do {
            let v10 = keys2;
            const v11 = v8;
            const v9 = v10[v11];
            v10 = v9;
        } while (v10 !== "b");
    }

    static changeCount(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4._listeners;
    }

    static testMemoization(param_0, param_1, param_2): void     {
        const v9 = Map;
        const map = new Map(v9);
        const v5 = 5;
        const v10 = map;
        let get = v10.get;
        get = v10.get(v5);
        get = get2;
        if (get === undefined) {
            let get = v5;
            const get2: number = get * v5;
            const v10 = map;
            const set = v10.set;
            let v11 = v5;
            const v12: number = get2;
        }
        const isUndefined: boolean = get === undefined;
        let v11 = String;
        v11 = v11(v6);
        const set: string = `${v11},`;
        v11 = String;
        v11 = isUndefined;
        return set + v11(v11);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testGenericStack(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v10: acc = acc;
        let v11 = "a";
        v10 = acc;
        push = v10.push;
        v11 = "b";
        v10 = acc;
        push = v10.push;
        v11 = "c";
        v10 = acc;
        let size = v10.size;
        size = v10.size();
        v10 = acc;
        peek = v10.peek();
        v10 = acc;
        pop = v10.pop();
        v10 = acc;
        size = v10.size;
        size = v10.size();
        let v15 = String;
        v15 = v15(size2);
        v11 = `${`${v15},` + peek2},`;
        v10 = v11 + pop2;
        size = `${v10},`;
        v10 = String;
        v11 = size3;
        return size + v10(v11);
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = onWindowStageCreate;
        v7 = Text;
        v5 = Text;
        v5 = Column;
        pop = v5.pop;
        return;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static testObservableValue(param_0, param_1, param_2): void     {
        new 10().value = 10;
        v5 = v3;
        v5.value = 20;
        v5 = v3;
        v5.value = 20;
        v5 = v3;
        v5.value = 30;
        let v7 = String;
        v7 = v7(v3.value);
        v5 = `${v7},`;
        v7 = String;
        value = v3;
        let changeCount = value.changeCount;
        changeCount = value.changeCount();
        return v5 + v7(changeCount);
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        let v7 = SubscriberManager;
        get = v7.Get();
        id__ = v10.id__();
        const get2 = v2;
        return;
    }

    static testArrayDestructure(param_0, param_1, param_2): void     {
        let v8: Array<unknown> = [];
        v8 = [];
        let v9 = 0;
        const v3 = v8[v9];
        v8 = v4;
        v9 = 1;
        const v5 = v8[v9];
        v8 = v4;
        v9 = 2;
        const v6 = v8[v9];
        v8 = String;
        v9 = v3 + v5;
        v9 += v6;
        return v8(v9);
    }

    static testPartialApplication(param_0, param_1, param_2): void     {
        const v3 = func_main_0;
        const v6 = anonymous_method;
        let v8 = v3;
        v8 = v8(5);
        v8 = v6;
        v9 = 5;
        v8 = v8(v9);
        let v10 = String;
        v10 = v10(v8);
        v8 = `${v10},`;
        v10 = String;
        return v8 + v82(v82);
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
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