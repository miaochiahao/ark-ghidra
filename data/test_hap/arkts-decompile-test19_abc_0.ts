export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw acc;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            return;
        }
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
        v8 = func_6;
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw acc;
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
        v2.value = v9;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     { }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v8 = lex_0_0;
        v7 = Text;
        message = 30;
        v7 = Text;
        message = FontWeight;
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     { }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static testArrayFill(param_0, param_1, param_2): void     {
        const v5 = Array;
        let v6 = 5;
        v6 = new 5(v5);
        let fill = v6.fill;
        fill = v6.fill(7);
        v7 = String;
        let v8 = fill;
        v8 = v8[v9];
        v7 = v7(v8);
        fill = `,${v7}`;
        v7 = String;
        v7 = fill;
        v8 = 4;
        v7 = v7[v8];
        return v7(v7) + fill;
    }

    static async testArrayFrom(param_0, param_1, param_2): void     {
        const v6 = Array;
        let from = v6.from;
        let v7: string[] = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        from = v6.from(v7);
        v7 = String;
        v7 = v7(from.length);
        from = `,${v7}`;
        v7 = String;
        v7 = from;
        length = 1;
        v7 = v7[length];
        return v7(v7) + from;
    }

    static testPromiseAll(param_0, param_1, param_2): void     {
        let v7 = Promise;
        let v8 = func_1;
        const func_1: func_1 = new func_1(v7, v8);
        v7 = Promise;
        v8 = func_5;
        const func_5: func_5 = new func_5(v7, v8, v9, v10, v11, v12);
        v7 = Promise;
        v8 = func_9;
        const func_9: func_9 = new func_9(v7, v8, v9, v10, v11, v12, v13, v14, v15, v16);
        return "Promise.all created";
    }

    static testReplaceAll(param_0, param_1, param_2): void     {
        const v4 = "aabbccaabb";
        const v7: string = v4;
        let replaceAll = v7.replaceAll;
        replaceAll = v7.replaceAll("aa", "XX");
        return replaceAll;
    }

    static testPromiseRace(param_0, param_1, param_2): void     {
        const v5 = Promise;
        const v6 = func_1;
        const func_1: func_1 = new func_1(v5, v6);
        return "Promise.race created";
    }

    static get getEntryName(): void     {
        return "Index";
    }

    static testArrayFlatten(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        let v7: string[] = ["/* element_0 */"];
        ["/* element_0 */"][6] = v15;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][6] = v16;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"][6] = v17;
        const v4: Array<unknown> = v6;
        v6 = [];
        const v3: Array<unknown> = [];
        v7 = 0;
        const v8: number = v7;
        const v12 = v8;
        while (!(v12.length < v11)) {
            const v12 = v3;
            let push = v12.push;
            let v13 = v8;
            const v14 = v10;
            v13 = v13[v14];
            push = v10;
            const number: number = Number(push);
            const v10: number = number + 1;
            continue;
        }
    }

    static testMapIteration(param_0, param_1, param_2): void     {
        const v5 = Map;
        const map = new Map(v5);
        let v6 = map;
        let set = v6.set;
        let v7 = "a";
        let v8 = 1;
        v6 = map;
        set = v6.set;
        v7 = "b";
        v8 = 2;
        v6 = map;
        set = v6.set;
        v7 = "c";
        v8 = 3;
        set = [];
        set = [];
        lex_0_0 = set;
        v6 = map;
        v7 = func_18;
        v7 = ",";
        return v6.join(v7);
    }

    static testStringConcat(param_0, param_1, param_2): void     {
        const v3 = "Hello";
        const v4 = " ";
        const v5 = "World";
        const v10: string = v3;
        const v9: number = v4 + v10;
        const v8: number = v5 + v9;
        return `!${v8}`;
    }

    static testTypeofGuards(param_0, param_1, param_2): void     {
        const v4: Array<unknown> = [];
        const v7 = 0;
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = func_8;
        v7 = Text;
        v5 = Text;
        v5 = Column;
        pop = v5.pop;
        return;
    }

    static testSetOperations(param_0, param_1, param_2): void     {
        const v5 = Set;
        let set = new Set(v5);
        let v6 = set;
        let add = v6.add;
        let v7 = 1;
        v6 = set;
        add = v6.add;
        v7 = 2;
        v6 = set;
        add = v6.add;
        v7 = 3;
        add = Set;
        set = new Set(add, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20);
        lex_0_0 = set;
        add = v6.add;
        v7 = 2;
        add = v6.add;
        v7 = 3;
        add = v6.add;
        v7 = 4;
        add = 0;
        lex_0_1 = add;
        v6 = set;
        let forEach = v6.forEach;
        v7 = func_32;
        forEach = Set;
        set = new Set(forEach, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40);
        lex_0_2 = set;
        v6 = set;
        forEach = v6.forEach;
        v7 = func_40;
        forEach = v6.forEach;
        v7 = func_45;
        v7 = String;
        v7 = v7(v8);
        forEach = `,${v7}`;
        v7 = String;
        const size = v7.size;
        return v7(size) + forEach;
    }

    static testStringPadding(param_0, param_1, param_2): void     {
        const v5 = "42";
        let v8: string = v5;
        let padStart = v8.padStart;
        padStart = v8.padStart(6, "0");
        v8 = v5;
        let padEnd = v8.padEnd;
        v9 = 6;
        v10 = "-";
        padEnd = v8.padEnd(v9, v10);
        v8 = padStart;
        padEnd = `|${v8}`;
        return padEnd + padEnd;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static testArrayCopyWithin(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        const v6: Array<unknown> = v3;
        let copyWithin = v6.copyWithin;
        copyWithin = v6.copyWithin(0, 3, 5);
        v7 = String;
        v8 = copyWithin;
        v9 = 0;
        v8 = v8[v9];
        v7 = v7(v8);
        copyWithin = `,${v7}`;
        v7 = String;
        v7 = copyWithin;
        v8 = 1;
        v7 = v7[v8];
        return v7(v7) + copyWithin;
    }

    static testLabeledContinue(param_0, param_1, param_2): void     {
        const v6 = 0;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        let v7 = SubscriberManager;
        get = v7.Get();
        id__ = v10.id__();
        get = v10;
        return;
    }

    static testNumberFormatting(param_0, param_1, param_2): void     {
        const v5 = 3.14159;
        let v8: number = v5;
        let toFixed = v8.toFixed;
        toFixed = v8.toFixed(2);
        v8 = v5;
        toFixed = v8.toFixed;
        v9 = 4;
        toFixed = v8.toFixed(v9);
        v8 = toFixed;
        toFixed = `|${v8}`;
        return toFixed + toFixed;
    }

    static testNestedPropertyChains(param_0, param_1, param_2): void     {
        let v8 = new 42();
        const v6 = new new 42()(v7, v8);
        const v3 = new new new 42()(v7, v8)(v5, v6, v7, v8);
        const v5 = String;
        const middle = v8.middle;
        const inner = middle.inner;
        const val = inner.val;
        return v5(val);
    }

    static testOptionalChainingChain(param_0, param_1, param_2): void     {
        let v7 = "hello";
        v7 = new "hello"();
    }

    static testNullishCoalescingChain(param_0, param_1, param_2): void     {
        const v3 = null;
        const v9: null = v3;
        const v10: null = v3;
        if (v10 === null) {
        } else {
            const v10 = v9;
        }
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