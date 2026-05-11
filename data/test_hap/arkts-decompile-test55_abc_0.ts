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
        v4.buffer = [];
        v4 = v8;
        v4.capacity = 5;
        v4 = v8;
        v4.producedCount = 0;
        v4 = v8;
        v4.consumedCount = 0;
        return v8;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
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

    static testBFS(param_0, param_1, param_2): void     {
        const v6 = Map;
        const map = new Map(v6);
        let v7 = map;
        let v8 = 0;
        let v9: string[] = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */"];
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */"];
        v7.set(v8, v9);
        v7 = map;
        set = v7.set;
        v8 = 1;
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */"];
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */"];
        v7.set(v8, v9);
        v7 = map;
        set = v7.set;
        v8 = 2;
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */"];
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */"];
        v7.set(v8, v9);
        v7 = map;
        set = v7.set;
        v8 = 3;
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */"];
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */"];
        v7.set(v8, v9);
        v7 = map;
        set = v7.set;
        v8 = 4;
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */", "/* element_20 */", "/* element_21 */", "/* element_22 */", "/* element_23 */", "/* element_24 */"];
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */", "/* element_20 */", "/* element_21 */", "/* element_22 */", "/* element_23 */", "/* element_24 */"];
        v7.set(v8, v9);
        set = lex_0_2;
        v7 = map;
        v8 = 0;
        set = set(v7, v8);
        v7 = set2;
        v8 = ",";
        v7.join(v8);
        return v7.join(v8);
    }

    static testTrie(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v10: acc = acc;
        v10.insert("apple");
        v10 = acc;
        insert = v10.insert;
        v11 = "app";
        v10.insert(v11);
        v10 = acc;
        insert = v10.insert;
        v11 = "application";
        v10.insert(v11);
        v10 = acc;
        v11 = "app";
        v10.search(v11);
        search = v10.search(v11);
        v10 = acc;
        search = v10.search;
        v11 = "apple";
        v10.search(v11);
        search = v10.search(v11);
        v10 = acc;
        search = v10.search;
        v11 = "appl";
        v10.search(v11);
        search = v10.search(v11);
        v10 = acc;
        let startsWith = v10.startsWith;
        v11 = "app";
        v10.startsWith(v11);
        startsWith = v10.startsWith(v11);
        let v15 = String;
        v15 = v15(search2);
        let v13: string = `${v15},`;
        v15 = String;
        v15 = search3;
        v11 = `${v13 + v15(v15)},`;
        v12 = String;
        v13 = search4;
        v10 = v11 + v12(v13);
        startsWith = `${v10},`;
        v10 = String;
        v11 = startsWith2;
        return startsWith + v10(v11);
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
        v0 = v17;
        lex_0_2 = func_main_0;
        v9 = anonymous_method;
        lex_0_0 = v9;
        v9 = func_main_0;
        lex_0_1 = v9;
        v9 = onCreate;
        lex_0_6 = v9;
        let v11 = ViewPU;
        let v11 = Reflect;
        let set = v11.set;
        v11.set(ViewPU.prototype, "finalizeConstruction", anonymous_method);
        prototype = undefined;
        let v0: @system.app = @system.app;
        v11 = set * undefined;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        let v0: @system.app = @system.app;
        v11 = set * undefined;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        let v0: @system.app = @system.app;
        v11 = set * undefined;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        v11 = set * undefined;
        prototype = set.prototype;
        v14 = "message";
        let v15 = undefined;
        false[v16] = "get".v15;
        v14 = "message";
        v15 = undefined;
        v16 = getEntryName;
        false[v15] = "get".v16;
        prototype2.initialRender = retryWithBackoff;
        prototype2.rerender = ProducerConsumer;
        set.getEntryName = setInitiallyProvidedValue;
        set = set;
        set = set;
        set = registerNamedRoute;
        v11 = func_49;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static partition(param_0, param_1, param_2, param_3, param_4, param_5): void     {
        v5 = v22;
        let v10 = v3;
        let v11 = v5;
        const v7 = v10[v11];
        v10 = v21;
        const v6: number = v10 - 1;
        v11 = v21;
        const v12 = v11;
        while (!(v12 < v5)) {
            const v13 = v3;
            let v14 = v11;
            const v12 = v13[v14];
            if (v12 < v7) {
                const v13 = v6;
                const number: number = Number(v13);
                const v6: number = _number + 1;
                let _number = v3;
                let v14: number = v6;
                const v12 = _number[v14];
                _number = v3;
                v14 = v6;
                const v15 = v3;
                const v16 = v11;
                _number[v14] = v15[v16];
                _number = v3;
                v14 = v11;
                _number[v14] = v12;
                const v12 = v11;
                const number: number = Number(v12);
                let v11: number = _number2 + 1;
                continue;
            }
        }
        let v10: boolean = v12 < v7;
        let _number2 = v6;
        let v11: number = _number2 + 1;
        const v8 = v10[v11];
        v10 = v20;
        _number2 = v6;
        v11 = _number2 + 1;
        _number2 = v20;
        v10[v11] = _number2[_number];
        v10 = v20;
        v11 = v5;
        v10[v11] = v8;
        v10 = v6;
        return v10 + 1;
    }

    static quickSort(param_0, param_1, param_2, param_3, param_4, param_5): void     {
        v5 = v17;
        let v8 = lex_0_0;
        v8 = v8(v15, v16, v5);
        v8 = lex_0_1;
        v9 = v15;
        v10 = v16;
        v11 = v8;
        v11--;
        v8 = lex_0_1;
        v9 = v15;
        v10 = v8;
        v10++;
        v11 = v5;
        return;
    }

    static testRetry(param_0, param_1, param_2): void     {
        const v4 = lex_0_6;
        let v5: Array<unknown> = [];
        v5 = [];
        return v4(v5);
    }

    static insert(param_0, param_1, param_2, param_3): void     {
        const v6 = v2;
        const root = v6.root;
        const v7 = 0;
        const v11 = v3;
        v11.charAt(v7);
        charAt = v11.charAt(v12);
        v12 = root;
        let children = v12.children;
        let get = children.get;
        v12 = charAt2;
        children.get(v12);
        get = children.get(v12);
        get = get2;
        if (!(get === undefined)) {
            const isUndefined: boolean = get === undefined;
            const charAt2 = v7;
            const number: number = Number(charAt2);
            const v7: number = _number + 1;
        }
        const acc: acc = new acc(get, children, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25);
        let v12 = isUndefined;
        children = v12.children;
        v12 = _number;
        const v13: acc = acc;
        children.set(v12, v13);
    }

    static search(param_0, param_1, param_2, param_3): void     {
        const v6 = v2;
        const root = v6.root;
        const v7 = 0;
        const v8 = v7;
        const v9 = v3;
        while (!(v8 < v9.length)) {
            const v11 = v3;
            let charAt = v11.charAt;
            let v12 = v7;
            v11.charAt(v12);
            charAt = v11.charAt(v12);
            v12 = root;
            const children = v12.children;
            let get = children.get;
            v12 = charAt2;
            children.get(v12);
            get = children.get(v12);
            get = get2;
            if (!(get === undefined)) {
                const isUndefined: boolean = get === undefined;
                const charAt2 = v7;
                const number: number = Number(charAt2);
                const v7: number = _number + 1;
                continue;
            }
            return false;
        }
        return v6.isEnd;
    }

    static consume(param_0, param_1, param_2): void     {
        const v7 = v2;
        let buffer = v7.buffer;
        const length = buffer.length;
        if (!(length === 0)) {
            const v7: boolean = length === 0;
            buffer = v7.buffer;
            let shift = buffer.shift;
            buffer.shift();
            shift = buffer.shift();
            buffer = v2;
            const consumedCount = buffer.consumedCount;
            const number: number = Number(consumedCount);
            buffer.consumedCount = _number + 1;
            return shift2;
        }
        const _number = 1;
        return -_number;
    }

    static produce(param_0, param_1, param_2, param_3): void     {
        const v7 = v2;
        let buffer = v7.buffer;
        const length = buffer.length;
        buffer = v10;
        if (!(length >= buffer.capacity)) {
            let hasCapacity: boolean = length >= buffer.capacity;
            buffer = hasCapacity.buffer;
            const push = buffer.push;
            hasCapacity = v3;
            buffer.push(hasCapacity);
            buffer = v2;
            const producedCount = buffer.producedCount;
            const number: number = Number(producedCount);
            buffer.producedCount = _number + 1;
            return true;
        }
        return false;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static bfsTraversal(param_0, param_1, param_2, param_3, param_4): void     {
        let v9 = Set;
        const set = new Set(v9);
        v9 = [];
        const v5: Array<unknown> = [];
        v9 = [];
        const v6: Array<unknown> = [];
        let v10 = set;
        v10.add(v21);
        v10 = v5;
        v11 = v21;
        v10.push(v11);
        v10 = v5;
        let v12 = v5;
        v12.shift();
        shift = v12.shift();
        v12 = v6;
        v12.push(shift2);
        v12 = v20;
        let get = v12.get;
        v13 = shift2;
        v12.get(v13);
        get = v12.get(v13);
        get = get2;
        let v12 = 0;
        let v13: number = v12;
        const v14 = get2;
        if (get !== undefined && v13 < v14.length) {
            const v14 = get2;
            let v15 = v12;
            let v13 = v14[v15];
            v15 = set;
            const has = v15.has;
            let v16 = v13;
            v15.has(v16);
            let v15 = set;
            const add = v15.add;
            let v16 = v13;
            v15.add(v16);
            v15 = v5;
            const push = v15.push;
            v16 = v13;
            v15.push(v16);
            let v13 = v12;
            const number: number = Number(v13);
            let v12: number = _number + 1;
        }
    }

    static getStatus(param_0, param_1, param_2): void     {
        let v8 = String;
        v8 = v8(v12.producedCount);
        const v6: string = `${v8},`;
        v8 = String;
        const consumedCount = v8.consumedCount;
        let v5: number = v6 + v8(consumedCount);
        const v4: string = `${v5},`;
        v5 = String;
        const buffer = v8.buffer;
        const length = buffer.length;
        return v4 + v5(length);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testQuickSort(param_0, param_1, param_2): void     {
        let v5: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v5 = lex_0_1;
        let v6: Array<unknown> = v3;
        let v7 = 0;
        const v9: Array<unknown> = v3;
        const length = v9.length;
        length--;
        v7 = ",";
        v3.join(v7);
        return v3.join(v7);
    }

    static startsWith(param_0, param_1, param_2, param_3): void     {
        const v6 = v2;
        const root = v6.root;
        const v7 = 0;
        do {
            const v11 = v3;
            let charAt = v11.charAt;
            let v12 = v7;
            v11.charAt(v12);
            charAt = v11.charAt(v12);
            v12 = root;
            const children = v12.children;
            let get = children.get;
            v12 = charAt2;
            children.get(v12);
            get = children.get(v12);
            get = get2;
        } while (get === undefined);
        return false;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static retryWithBackoff(param_0, param_1, param_2, param_3): void     {
        const v5: Array<unknown> = [];
        const v4 = 100;
        const v8 = 0;
        const v9: number = v8;
        let v10 = v3;
        let v10 = v3;
        const v11 = v8;
        const v9 = v10[v11];
        if (!(v10 === 0)) {
            const v11: boolean = v10 === 0;
            let push = v11.push;
            const v12 = "fail:";
            const v13 = String;
            const v14 = v4;
            v12 += v13(v14);
            v11.push(v12);
            push = v4;
            const v4: number = push * 2;
            push = v4;
            if (!(push > 1000)) {
                const v9: boolean = push > 1000;
                const number: number = Number(v9);
                const v8: number = _number + 1;
            }
            const v4 = 1000;
        }
        const v11 = v5;
        push = v11.push;
        v11.push("ok");
        let push = v11.push(v12);
        push2.join(",");
        return push2.join(_number);
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

    static testProducerConsumer(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v10: acc = acc;
        v10.produce(10);
        v10 = acc;
        produce = v10.produce;
        v11 = 20;
        v10.produce(v11);
        v10 = acc;
        produce = v10.produce;
        v11 = 30;
        v10.produce(v11);
        v10 = acc;
        v10.consume();
        consume = v10.consume();
        v10 = acc;
        produce = v10.produce;
        v11 = 40;
        v10.produce(v11);
        v10 = acc;
        consume = v10.consume;
        v10.consume();
        consume = v10.consume();
        v10 = acc;
        consume = v10.consume;
        v10.consume();
        consume = v10.consume();
        v10 = acc;
        let status = v10.getStatus;
        v10.getStatus();
        status = v10.getStatus();
        let v15 = String;
        v15 = v15(consume2);
        let v13: string = `${v15},`;
        v15 = String;
        v15 = consume3;
        v11 = `${v13 + v15(v15)},`;
        v12 = String;
        v13 = consume4;
        v10 = v11 + v12(v13);
        status = `${v10}|`;
        return status + status2;
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