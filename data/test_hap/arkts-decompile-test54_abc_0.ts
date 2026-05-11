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

    constructor(param_0, param_1, param_2, param_3)     {
        v2 = v11;
        v2.capacity = 0;
        v5 = v2;
        v5.cache = new Map(Map, v7, v8);
        v5 = v2;
        let v7 = "";
        let v8 = 0;
        v5.head = new Map(v7, v8, v9, v10, v11, v12);
        v5 = v2;
        v7 = "";
        v8 = 0;
        v5.tail = new Map(v7, v8, v9, v10, v11, v12, v13, v14, v15, v16);
        v5 = v2;
        v5.capacity = v12;
        v6 = v2;
        const head = v6.head;
        v6 = v2;
        head.next = v6.tail;
        const tail = v6.tail;
        tail.prev = v2.head;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static merge(param_0, param_1, param_2, param_3, param_4): void     {
        let v9: Array<unknown> = [];
        const v7: Array<unknown> = [];
        const v5 = 0;
        const v6 = 0;
        v9 = v5;
        let v10 = v3;
        let v9 = v6;
        let v10 = v4;
        v10 = v16;
        v9 = v10[v11];
        v10 = v17;
        v11 = v6;
        if (!(v9 <= v10[v11])) {
            let v10: boolean = v9 <= v10[v11];
            let push = v10.push;
            let v11 = v4;
            const v12 = v6;
            v11 = v11[v12];
            v10.push(v11);
            push = v6;
            const number: number = Number(push);
            const v6: number = _number + 1;
        }
        let v10 = v7;
        let push = v10.push;
        let v11 = v3;
        v11 = v11[v12];
        v10.push(v11);
        push = v5;
        const number: number = Number(push);
        const v5: number = _number + 1;
        const _number = _number;
        let v10 = v3;
        while (!(_number < v10.length)) {
            loop_0: do {
    let hasLength: boolean = _number < v10.length;
    let v10 = v3;
} while (hasLength < v10.length);
        }
        const hasLength: boolean = hasLength < v10.length;
        let v10 = v4;
        while (!(hasLength < v10.length)) {
            loop_1: do {
    let hasLength: boolean = hasLength < v10.length;
    let v10 = v4;
} while (hasLength < v10.length);
        }
        return hasLength < v10.length;
    }

    static get(param_0, param_1, param_2, param_3): void     {
        let v8 = v2;
        const cache = v8.cache;
        let get = cache.get;
        cache.get(v12);
        get = cache.get(v12);
        get = get2;
        if (!(get === undefined)) {
            let isUndefined: boolean = get === undefined;
            const removeNode = isUndefined.removeNode;
            let v8 = get2;
            isUndefined.removeNode(v8);
            isUndefined = v2;
            let addToHead = isUndefined.addToHead;
            v8 = get2;
            isUndefined.addToHead(v8);
            addToHead = get2;
            return addToHead.value;
        }
        let addToHead = 1;
        return -addToHead;
    }

    static put(param_0, param_1, param_2, param_3, param_4): void     {
        let v9 = v2;
        let cache = v9.cache;
        let get = cache.get;
        v9 = v15;
        cache.get(v9);
        get = cache.get(v9);
        get = get2;
        if (!(get !== undefined)) {
            let v9 = v3;
            let v10 = v4;
            v4 = new v4(cache, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24);
            v10 = v2;
            let cache = v10.cache;
            const set = cache2.set;
            v10 = v3;
            let v11: v4 = v4;
            cache2.set(v10, v11);
            const cache2 = v2;
            const addToHead = cache2.addToHead;
            v10 = v4;
            cache2.addToHead(v10);
            v10 = v2;
            cache = v10.cache;
            const size = cache.size;
            cache = v2;
            let v10 = v2;
            const tail = v10.tail;
            let prev = tail.prev;
            v10 = v2;
            let removeNode = v10.removeNode;
            let v11 = prev;
            v10.removeNode(v11);
            v11 = v2;
            cache = v11.cache;
            const delete = cache.delete;
            v11 = prev;
            const key = v11.key;
            cache.delete(key);
            return;
        }
        let prev = v2;
        prev.removeNode(get2);
        removeNode = get2;
        removeNode.value = v16;
        prev = v14;
        _delete = get2;
        prev.addToHead(_delete);
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

    static testBST(param_0, param_1, param_2): void     {
        const v5 = null;
        let v8 = lex_0_3;
        v8 = v8(v5, 5);
        v8 = lex_0_3;
        v9 = v8;
        v10 = 3;
        v8 = v8(v9, v10);
        v8 = lex_0_3;
        v9 = v8;
        v10 = 7;
        v8 = v8(v9, v10);
        v8 = lex_0_3;
        v9 = v8;
        v10 = 1;
        v8 = v8(v9, v10);
        v8 = lex_0_3;
        v9 = v8;
        v10 = 4;
        v8 = v8(v9, v10);
        v8 = lex_0_4;
        v9 = v8;
        v10 = 3;
        v8 = v8(v9, v10);
        v8 = lex_0_4;
        v9 = v8;
        v10 = 6;
        v8 = v8(v9, v10);
        v8 = lex_0_5;
        v9 = v8;
        v8 = v8(v9);
        let v12 = String;
        v12 = v12(v82);
        v10 = `${v12},`;
        v12 = String;
        v12 = v83;
        v9 = v10 + v12(v12);
        v8 = `${v9},`;
        v12 = ",";
        v84.join(v12);
        return v8 + v84.join(v12);
    }

    static testLRU(param_0, param_1, param_2): void     {
        let v10 = 3;
        v10 = new 3();
        v10.put("a", 1);
        v10 = v3;
        put = v10.put;
        v11 = "b";
        v12 = 2;
        v10.put(v11, v12);
        v10 = v3;
        put = v10.put;
        v11 = "c";
        v12 = 3;
        v10.put(v11, v12);
        v10 = v3;
        let get = v10.get;
        v11 = "a";
        v10.get(v11);
        get = v10.get(v11);
        v10 = v3;
        put = v10.put;
        v11 = "d";
        v12 = 4;
        v10.put(v11, v12);
        v10 = v3;
        get = v10.get;
        v11 = "b";
        v10.get(v11);
        get = v10.get(v11);
        v10 = v3;
        get = v10.get;
        v11 = "c";
        v10.get(v11);
        get = v10.get(v11);
        v10 = v3;
        get = v10.get;
        v11 = "d";
        v10.get(v11);
        get = v10.get(v11);
        v12 = `${get2},` + get3;
        v11 = `${v12},`;
        v10 = v11 + get4;
        get = `${v10},`;
        return get + get5;
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
        v1 = v18;
        v2 = v19;
        lex_0_5 = func_main_0;
        v9 = anonymous_method;
        lex_0_3 = v9;
        v9 = func_main_0;
        lex_0_8 = v9;
        v9 = onCreate;
        lex_0_1 = v9;
        v9 = onDestroy;
        lex_0_0 = v9;
        v9 = EntryAbility;
        lex_0_9 = v9;
        v9 = onBackground;
        lex_0_4 = v9;
        let v11 = ViewPU;
        let v11 = Reflect;
        let set = v11.set;
        v11.set(ViewPU.prototype, "finalizeConstruction", func_main_0);
        prototype = undefined;
        let v1: @system.app = @system.app;
        v11 = set * undefined;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        let v2: @system.app = @system.app;
        v11 = set * undefined;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = undefined;
        let v1: @system.app = @system.app;
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
        v16 = removeDuplicates;
        false[v15] = "get".v16;
        prototype2.initialRender = initialRender;
        prototype2.rerender = aboutToBeDeleted;
        set.getEntryName = method_48;
        set = set;
        set = set;
        set = registerNamedRoute;
        v11 = func_52;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static insertBST(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        if (!(v6 === null)) {
            const isNull: boolean = v6 === null;
            const v7 = v3;
            if (isNull < v7.value) {
                const isNull = v3;
                const v7 = lex_0_3;
                const v8 = v3;
                const left = v8.left;
                const v9 = v4;
                isNull.left = v7(left, v9);
            } else {
                const hasValue: boolean = isNull < v7.value;
                const v7 = lex_0_3;
                const left = v3;
                const right = left.right;
                const v9 = v4;
                hasValue.right = v7(right, v9);
            }
            return v3;
        }
        const v7 = v4;
        return new v14(hasValue);
    }

    static mergeSort(param_0, param_1, param_2, param_3): void     {
        const v9 = v3;
        return v16;
    }

    static searchBST(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        if (!(v6 === null)) {
            const isNull: boolean = v6 === null;
            const value = isNull.value;
            if (!(value === v4)) {
                const value: boolean = value === v4;
                const isNull = v3;
                if (!(value < isNull.value)) {
                    const hasValue: boolean = value < isNull.value;
                    const isNull = v3;
                    const right = isNull.right;
                    const v8 = v4;
                    return hasValue(right, v8);
                }
                const hasValue = lex_0_4;
                const right = v3;
                const left = right.left;
                const v8 = v4;
                return hasValue(left, v8);
            }
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

    static addToHead(param_0, param_1, param_2, param_3): void     {
        v3 = v11;
        v3.prev = v10.head;
        v5 = v3;
        v5.next = v10.head.next;
        v7 = v10;
        head = v7.head;
        const next = head.next;
        if (!(next !== null)) {
            const isNull: boolean = next !== null;
            let head = isNull.head;
            head.next = v3;
            return;
        }
        let v7 = v2;
        let head = v7.head;
        const next = head2.next;
        next.prev = v3;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testMergeSort(param_0, param_1, param_2): void     {
        let v5 = lex_0_0;
        let v6: Array<unknown> = [];
        v6 = [];
        v5 = v5(v6);
        v5.join(",");
        return v5.join(v7);
    }

    static removeNode(param_0, param_1, param_2, param_3): void     {
        let v7 = v3;
        const prev = v7.prev;
        v7 = v11;
        const next = v7.next;
        v7 = prev;
        if (v7 !== null) {
            let v7 = prev;
            v7.next = next;
            let v7 = next;
        }
        let v7 = next;
        v7.prev = prev;
        return;
    }

    static maxSubarraySum(param_0, param_1, param_2, param_3, param_4): void     {
        const v9 = v3;
        return 0;
    }

    static testTwoPointer(param_0, param_1, param_2): void     {
        let v7: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v7 = lex_0_9;
        v7 = v7(v3);
        v7 = [];
        v8 = 0;
        const v9 = v8;
        while (!(v9 < v7)) {
            loop_0: do {
    let v9: boolean = v9 < v7;
} while (v9 < v7);
        }
        let v8: boolean = v9 < v7;
        const v9 = ",";
        v8.join(v9);
        return v8.join(v9);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static inorderTraversal(param_0, param_1, param_2, param_3): void     {
        let v6: Array<unknown> = [];
        let v8 = lex_0_5;
        v8 = v8(v17.left);
        left = 0;
        const v10 = left;
        const v11 = v8;
        while (!(v10 < v11.length)) {
            loop_0: do {
    let hasLength: boolean = v10 < v11.length;
    let v11 = v8;
} while (hasLength < v11.length);
        }
        let hasLength: boolean = hasLength < v11.length;
        hasLength = v17;
        hasLength2.push(hasLength.value);
        push = lex_0_5;
        const hasLength2 = v3;
        push = push(hasLength2.right);
        right = 0;
        push = right.push;
        let value = v3;
        value = value.value;
        right.push(value);
        push = lex_0_5;
        right = v17;
        right = right.right;
        push = push(right);
        right = 0;
        let push = v11.push;
        let v12 = push2;
        v12 = v12[v13];
        v11.push(v12);
        const push3 = right;
        const number: number = Number(push3);
        let right: number = _number + 1;
        for (let right = right; !(value < v11.length); v11) {
            let value = right;
            const v11 = push2;
        }
        return _number;
    }

    static removeDuplicates(param_0, param_1, param_2, param_3): void     {
        const v7 = v3;
        return 0;
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

    static testSlidingWindow(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v6 = lex_0_8;
        v6 = v6(v3, 3);
        v6 = String;
        return v6(v6);
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