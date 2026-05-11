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

    constructor(param_0, param_1, param_2, param_3, param_4)     {
        v2 = v9;
        v2._owner = v10;
        v6 = v2;
        v6._balance = v11;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static sum(param_0, param_1, param_2): void     {
        let v5 = v2;
        const first = v5.first;
        return v8.second + first;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static swap(param_0, param_1, param_2): void     {
        const v6 = v2;
        const second = v6.second;
        const v7 = v2;
        const first = v7.first;
        return new v7.first(v4, second, first, v7);
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
        lex_0_4 = func_0;
        throw acc;
    }

    static colorName(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(acc === v5)) {
        }
    }

    static deposit(param_0, param_1, param_2, param_3): void     {
        v2 = v10;
        v2._balance = v11 + v2._balance;
        return;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static getOwner(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4._owner;
    }

    static withdraw(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        const v6 = v2;
        if (!(v6._balance > v5)) {
            const v7 = v2;
            const _balance = v7._balance;
            v5._balance = v3 - _balance;
            return true;
        }
    }

    static toString(param_0, param_1, param_2): void     {
        let v7 = "(";
        const v8 = String;
        const v9 = v2;
        const first = v9.first;
        let v6: number = v8(first) + v7;
        const v5: string = `,${v6}`;
        v6 = String;
        const second = v7.second;
        const v4: number = v6(second) + v5;
        return `)${v4}`;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static testPairSwap(param_0, param_1, param_2): void     {
        let v7 = 10;
        let v8 = 20;
        v7 = new 20();
        let swap = v7.swap;
        swap = v7.swap();
        const v11 = v3;
        let toString = v11.toString;
        toString = v11.toString();
        v8 = `->${toString}`;
        toString = swap;
        toString = toString.toString;
        v7 = toString.toString() + v8;
        swap = `=${v7}`;
        v7 = String;
        toString = swap;
        let sum = toString.sum;
        sum = toString.sum();
        return v7(sum) + swap;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static testColorEnum(param_0, param_1, param_2): void     {
        let v8 = lex_0_4;
        v8 = v8(v9);
        let v6: string = `,${v8}`;
        v8 = lex_0_4;
        let v5: number = v8(v8) + v6;
        const v4: string = `,${v5}`;
        v5 = lex_0_4;
        v6 = 3;
        return v5(v6) + v4;
    }

    static getBalance(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4._balance;
    }

    static testArrayGuard(param_0, param_1, param_2, param_3): void     { }

    static testBankAccount(param_0, param_1, param_2): void     {
        let v7 = "Alice";
        let v8 = 1000;
        v7 = new 1000();
        v8 = 500;
        v7 = v3;
        let withdraw = v7.withdraw;
        v8 = 200;
        withdraw = v7.withdraw(v8);
        let v11 = v3;
        let owner = v11.getOwner;
        owner = v11.getOwner();
        v8 = `:${owner}`;
        owner = String;
        let balance = v11.getBalance;
        balance = v3.getBalance();
        v7 = owner(balance) + v8;
        withdraw = `:${v7}`;
        v7 = String;
        v8 = withdraw;
        return v7(v8) + withdraw;
    }

    static get getEntryName(): void     {
        return "Index";
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

    static testBitwiseShifts(param_0, param_1, param_2): void     {
        let v10 = 1;
        const v3: number = 4 << v10;
        v10 = 64;
        const v4: number = 2 >> v10;
        v10 = -1;
        const v5: number = 0 >>> v10;
        v10 = v3;
        const v6: number = v4 | v10;
        v10 = v3;
        const v7: number = 15 & v10;
        v10 = v3;
        const v8: number = v4 ^ v10;
        let v20 = String;
        v20 = v20(v3);
        let v18: string = `,${v20}`;
        v20 = String;
        let v17: number = v20(v20) + v18;
        let v16: string = `,${v17}`;
        v17 = String;
        let v15: number = v17(v18) + v16;
        let v14: string = `,${v15}`;
        v15 = String;
        v16 = v6;
        let v13: number = v15(v16) + v14;
        let v12: string = `,${v13}`;
        v13 = String;
        v14 = v7;
        v11 = v13(v14) + v12;
        v10 = `,${v11}`;
        v11 = String;
        v12 = v8;
        return v11(v12) + v10;
    }

    static testCompoundAssign(param_0, param_1, param_2): void     {
        let v3 = 10;
        v3 = 5 + v3;
        v5 = v3;
        v3 = 3 - v5;
        v5 = v3;
        v3 = 2 * v5;
        v3 = 4 / v3;
        v3 = 7 % v3;
        return v3;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static testComplexTemplate(param_0, param_1, param_2): void     {
        const v4 = 42;
        const v5 = 3;
        let v7 = "x=";
        v7 = v4 + v7;
        v7 = `,y=${v7}`;
        v7 = v5 + v7;
        v7 = `,sum=${v7}`;
        const v8 = String;
        let v9: number = v4;
        v9 = v5 + v9;
        v7 = v8(v9) + v7;
        return `${v7}`;
    }

    static testObjectIteration(param_0, param_1, param_2): void     {
        {  }[95] = "math";
        v8 = v5;
        v9 = "english";
        v8[87] = v9;
        v8 = v5;
        v9 = "science";
        v8[92] = v9;
        v9 = Object;
        let keys = v9.keys;
        keys = v9.keys(v5);
        const v6 = 0;
        v9 = 0;
        let v11 = keys;
        while (!(v11.length < v10)) {
            let v11 = keys;
            let v12 = v9;
            let v10 = v11[v12];
            v11 = v6;
            v12 = v5;
            const v13 = v10;
            const v6: number = v12[v13] + v11;
            v10 = v9;
            const number: number = Number(v10);
            let v9: number = number + 1;
            continue;
        }
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