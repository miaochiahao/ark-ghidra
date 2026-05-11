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
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
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
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
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
        const v4 = v2;
        v4.items = [];
        return v8;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v9 = v4;
        let v8: string = `=${v9}`;
        v9 = String;
        v8 = v9(v14) + v8;
        v7.push(v8);
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        return v11 - v6;
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

    static isAlpha(param_0, param_1, param_2, param_3): void     {
        let v7 = v3;
        v7.charCodeAt(0);
        charCodeAt = v7.charCodeAt(v8);
        v8 = charCodeAt2;
        v7 = v8 <= 65;
        let hasCharCodeAt2: boolean = [] != charCodeAt2;
        let hasCharCodeAt2 = {  };
        let v7: boolean = hasCharCodeAt2 <= 97;
        hasCharCodeAt2 = acc != charCodeAt2;
    }

    static peek(param_0, param_1, param_2): void     {
        let v5 = v2;
        const items = v5.items;
        v5 = 0;
        return items[v5];
    }

    static size(param_0, param_1, param_2): void     {
        const v5 = v2;
        const items = v5.items;
        return items.length;
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
        v0 = v19;
        lex_0_2 = func_main_0;
        v11 = anonymous_method;
        lex_0_1 = v11;
        v11 = onWindowStageDestroy;
        lex_0_3 = v11;
        let v13 = ViewPU;
        let v13 = Reflect;
        let set = v13.set;
        v13.set(ViewPU.prototype, "finalizeConstruction", Index);
        prototype = undefined;
        let v0: @system.app = @system.app;
        v13 = undefined % set;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        v13 = undefined % set;
        prototype = set.prototype;
        v16 = "message";
        let v17 = undefined;
        false[v18] = "get".v17;
        v16 = "message";
        v17 = undefined;
        v18 = circularAccess;
        false[v17] = "get".v18;
        prototype2.initialRender = testMapEntries;
        prototype2.rerender = PriorityQueue;
        set.getEntryName = testSliceNegative;
        set = set;
        set = set;
        set = registerNamedRoute;
        v13 = setInitiallyProvidedValue;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static transform(param_0, param_1, param_2, param_3): void     {
        const v10 = v3;
        let trim = v10.trim;
        v10.trim();
        trim = v10.trim();
        trim2.toLowerCase();
        toLowerCase = trim2.toLowerCase();
        toLowerCase = RegExp;
        let trim2 = "\\s+";
        trim = "g";
        const regex = new RegExp(trim2, trim, v10, v11, v12, v13, v14, v15, v16, v17);
        trim2 = "_";
        toLowerCase2.replace(regex, trim2);
        return toLowerCase2.replace(regex, trim2);
    }

    static dequeue(param_0, param_1, param_2): void     {
        const v6 = v2;
        const items = v6.items;
        items.shift();
        return items.shift();
    }

    static enqueue(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        let items = v7.items;
        v7 = v11;
        items.push(v7);
        v7 = v10;
        items = v7.items;
        v7 = func_main_0;
        items.sort(v7);
        return;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static testIsAlpha(param_0, param_1, param_2): void     {
        let v8 = lex_0_1;
        v8 = v8("A");
        v8 = lex_0_1;
        v9 = "z";
        v8 = v8(v9);
        v8 = lex_0_1;
        v9 = "5";
        v8 = v8(v9);
        v8 = lex_0_1;
        v9 = "!";
        v8 = v8(v9);
        let v14 = String;
        v14 = v14(v8);
        let v12: string = `,${v14}`;
        v14 = String;
        let v11: number = v14(v14) + v12;
        let v10: string = `,${v11}`;
        v11 = String;
        v9 = v11(v83) + v10;
        v8 = `,${v9}`;
        v9 = String;
        v10 = v84;
        return v9(v10) + v8;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testTransform(param_0, param_1, param_2): void     {
        let v6 = lex_0_3;
        v6 = v6("  Hello World  ");
        v6 = lex_0_3;
        v7 = "  Foo   Bar  ";
        v6 = v6(v7);
        v6 = `|${v6}`;
        return v62 + v6;
    }

    static circularAccess(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = v4;
        v7 = v12.length % v7;
        if (!(v7 > 0)) {
            let v7: boolean = v7 > 0;
            const v8 = v5;
            return v7[v8];
        }
        let v7 = v5;
        const v8 = v3;
        const v5: number = v8.length + v7;
    }

    static testMapEntries(param_0, param_1, param_2): void     {
        const v5 = Map;
        const map = new Map(v5);
        let v6 = map;
        v6.set("x", 10);
        v6 = map;
        set = v6.set;
        v7 = "y";
        v8 = 20;
        v6.set(v7, v8);
        v6 = map;
        set = v6.set;
        v7 = "z";
        v8 = 30;
        v6.set(v7, v8);
        set = [];
        set = [];
        set = set;
        v6 = map;
        let forEach = v6.forEach;
        v7 = anonymous_method;
        v6.forEach(v7);
        v7 = String;
        v7 = v7(v8.length);
        forEach = `:${v7}`;
        length = ",";
        v7.join(length);
        return v7.join(length) + forEach;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testObjectValues(param_0, param_1, param_2): void     {
        {  }[v8] = 90;
        v7 = v3;
        v8 = "english";
        v7[v8] = 85;
        v7 = v3;
        v8 = "science";
        v7[v8] = 95;
        v8 = Object;
        v8.values(v3);
        values = v8.values(v9);
        const v4 = 0;
        v8 = 0;
        do {
            let v9 = v4;
            const v10 = values2;
            const v11 = v8;
            const v4: number = v10[v11] + v9;
            v9 = v8;
            const number: number = Number(v9);
            let v8: number = _number + 1;
        } while (_number);
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

    static testPriorityQueue(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v9: acc = acc;
        v9.enqueue(30);
        v9 = acc;
        enqueue = v9.enqueue;
        v10 = 10;
        v9.enqueue(v10);
        v9 = acc;
        enqueue = v9.enqueue;
        v10 = 20;
        v9.enqueue(v10);
        v9 = acc;
        enqueue = v9.enqueue;
        v10 = 5;
        v9.enqueue(v10);
        v9 = acc;
        v9.dequeue();
        dequeue = v9.dequeue();
        v9 = acc;
        v9.peek();
        peek = v9.peek();
        v9 = acc;
        let size = v9.size;
        v9.size();
        size = v9.size();
        let v12 = String;
        v12 = v12(dequeue2);
        v10 = `,${v12}`;
        v12 = String;
        v12 = peek2;
        v9 = v12(v12) + v10;
        size = `,${v9}`;
        v9 = String;
        v10 = size2;
        return v9(v10) + size;
    }

    static testSliceNegative(param_0, param_1, param_2): void     {
        const v5 = "Hello, World!";
        let v8: string = v5;
        let slice = v8.slice;
        let v9 = 5;
        v9 = -v9;
        v8.slice(v9);
        slice = v8.slice(v9);
        v8 = v5;
        slice = v8.slice;
        v9 = 7;
        v8.slice(v9, 12);
        slice = v8.slice(v9, v10);
        v8 = slice2;
        slice = `,${v8}`;
        return slice3 + slice;
    }

    static testCircularAccess(param_0, param_1, param_2): void     {
        let v8: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v8 = lex_0_2;
        v8 = v8(v3, 7);
        v8 = lex_0_2;
        v9 = v3;
        v10 = 0;
        v8 = v8(v9, v10);
        v8 = lex_0_2;
        v9 = v3;
        v10 = 4;
        v8 = v8(v9, v10);
        v10 = `,${v8}`;
        v9 = v82 + v10;
        v8 = `,${v9}`;
        return v83 + v8;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
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