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
        return v6;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static add(param_0, param_1, param_2, param_3): void     {
        v5.result = v7.result + v11;
        return v10;
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

    static start(param_0, param_1, param_2, param_3): void     {
        v5.result = v9;
        return new acc(v5, v6);
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
        lex_0_0 = func_main_0;
        v11 = anonymous_method;
        lex_0_1 = v11;
        v11 = func_main_0;
        lex_0_2 = v11;
        v11 = onCreate;
        lex_0_3 = v11;
        let v13 = ViewPU;
        let v13 = Reflect;
        let set = v13.set;
        v13.set(ViewPU.prototype, "finalizeConstruction", message);
        try {
        } catch (e) {
            throw acc;
        }
        set = set;
        let v13: number = set % undefined;
        prototype = set.prototype;
        let v16 = "message";
        let v17 = undefined;
        false[v18] = "get".v17;
        v16 = "message";
        v17 = undefined;
        v18 = getEntryName;
        false[v17] = "get".v18;
        prototype2.initialRender = testBinarySearch;
        prototype2.rerender = testMatrixTranspose;
        set.getEntryName = setInitiallyProvidedValue;
        set = set;
        set = registerNamedRoute;
        v13 = func_48;
        prototype = "";
        let prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static bubbleSort(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const length = v6.length;
        const v7 = 0;
        const v9 = length;
        const v9 = 0;
        const v10: number = v9;
        let v12 = length;
        let v11: number = v12 - v7;
        let v11 = v3;
        let v12 = v9;
        const v10 = v11[v12];
        v11 = v19;
        v12 = v9 + 1;
        if (!(v10 > v11[v12])) {
            const v10: boolean = v10 > v11[v12];
            const number: number = Number(v10);
            const v9: number = _number + 1;
        }
        let v11 = v3;
        let v12 = v9;
        const _number = v11[v12];
        v11 = v19;
        v12 = v9;
        let v13 = v3;
        v11[v12] = v13[v14];
        v11 = v19;
        v12 = v9 + 1;
        v11[v12] = _number;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static getValue(param_0, param_1, param_2): void     {
        return v4.result;
    }

    static multiply(param_0, param_1, param_2, param_3): void     {
        v5.result = v7.result * v11;
        return v10;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static binarySearch(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = 0;
        const v9 = v3;
        let length = v9.length;
        const v5: number = length - 1;
        length = v6;
        const v10 = v6;
        let v12 = v5;
        let v11: number = v12 - v6;
        let length: number = v10 + v11 / 2;
        v11 = Math;
        let floor = v11.floor;
        v12 = length;
        v11.floor(v12);
        floor = v11.floor(v12);
        floor = v16;
        v11 = floor2;
        const v9 = floor[v11];
        floor = v9;
        if (!(floor === v4)) {
            let floor: boolean = floor === v4;
            if (!(floor < v4)) {
                let floor: boolean = floor < v4;
                const v5: number = floor - 1;
            }
            let floor = floor2;
            const v6: number = floor + 1;
        }
        return floor2;
    }

    static isPalindrome(param_0, param_1, param_2, param_3): void     {
        const v4 = 0;
        const v8 = v3;
        const length = v8.length;
        const v5: number = length - 1;
        do {
            let v10 = v3;
            let charAt = v10.charAt;
            let v11 = v4;
            v10.charAt(v11);
            charAt = v10.charAt(v11);
            v10 = v3;
            charAt = v10.charAt;
            v11 = v5;
            v10.charAt(v11);
            charAt = v10.charAt(v11);
            charAt = charAt2;
        } while (charAt !== charAt3);
        return false;
    }

    static testUnaryOps(param_0, param_1, param_2, param_3): void     {
        let v8 = v3;
        const v5: number = -v8;
        v8 = v17;
        v8 = v8 > 0;
        let v8 = v3;
        v8 = v8 !== 0;
        let v12 = String;
        v12 = v12(v5);
        let v10: string = `${v12},`;
        v12 = String;
        let v9: number = v10 + v12(v12);
        let v8: string = `${v9},`;
        v9 = String;
        v10 = v4;
        return v8 + v9(v10);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static fibonacciMemo(param_0, param_1, param_2, param_3, param_4): void     {
        const v9 = v4;
        v9.get(v16);
        get = v9.get(v10);
        get = get2;
        return get2;
    }

    static testBubbleSort(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v6 = lex_0_1;
        v6 = v6(v3);
        let v10 = String;
        let v11: Array<unknown> = v6;
        v11 = v11[v12];
        v10 = v10(v11);
        let v8: string = `${v10},`;
        v10 = String;
        v10 = v6;
        v11 = 3;
        v10 = v10[v11];
        v7 = v8 + v10(v10);
        v6 = `${v7},`;
        v7 = String;
        v8 = v6;
        v10 = 6;
        v8 = v8[v10];
        return v6 + v7(v8);
    }

    static testPalindrome(param_0, param_1, param_2): void     {
        let v7 = lex_0_3;
        v7 = v7("racecar");
        v7 = lex_0_3;
        v8 = "hello";
        v7 = v7(v8);
        v7 = lex_0_3;
        v8 = "madam";
        v7 = v7(v8);
        let v11 = String;
        v11 = v11(v7);
        let v9: string = `${v11},`;
        v11 = String;
        v8 = v9 + v72(v72);
        v7 = `${v8},`;
        v8 = String;
        return v7 + v8(v73);
    }

    static testChainedCalc(param_0, param_1, param_2): void     {
        let start = v12.start;
        v12.start(5);
        start = v12.start(v13);
        let add = start2.add;
        start = 3;
        start2.add(start);
        add = start2.add(start);
        add = 2;
        add2.multiply(add);
        multiply = add2.multiply(add);
        let value = multiply2.getValue;
        multiply2.getValue();
        value = multiply2.getValue();
        value = String;
        const multiply2 = value2;
        return value(multiply2);
    }

    static testMemoization(param_0, param_1, param_2): void     {
        let v6 = Map;
        const map = new Map(v6);
        v6 = lex_0_2;
        v6 = v6(10, map);
        v6 = "fib(10)=";
        v7 = String;
        return v6 + v7(v6);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testBinarySearch(param_0, param_1, param_2): void     {
        let v8: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v8 = lex_0_0;
        v8 = v8(v3, 5);
        v8 = lex_0_0;
        v9 = v3;
        v10 = 8;
        v8 = v8(v9, v10);
        v8 = lex_0_0;
        v9 = v3;
        v10 = 13;
        v8 = v8(v9, v10);
        let v12 = String;
        v12 = v12(v8);
        v10 = `${v12},`;
        v12 = String;
        v9 = v10 + v82(v82);
        v8 = `${v9},`;
        v9 = String;
        return v8 + v9(v83);
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

    static testMatrixTranspose(param_0, param_1, param_2): void     {
        let v9: Array<unknown> = [];
        let v10: string[] = ["/* element_0 */"];
        ["/* element_0 */"][9] = v19;
        v10 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][9] = v20;
        const v5 = 2;
        v9 = [];
        v10 = 0;
        const v11: number = v10;
        const v11: Array<unknown> = [];
        const v13 = 0;
        const v14 = v13;
        while (!(v14 < v5)) {
            loop_0: do {
    let v14: boolean = v14 < v5;
} while (v14 < v5);
        }
        const v13: boolean = v14 < v5;
        const v14 = v11;
        v13.push(v14);
        const v11 = v10;
        const number: number = Number(v11);
        let v10: number = _number + 1;
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

    static static_initializer(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.result = 0;
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